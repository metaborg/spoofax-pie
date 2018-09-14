package mb.spoofax.runtime.jsglr;

import mb.spoofax.api.message.Message;
import mb.spoofax.api.message.Severity;
import mb.spoofax.api.region.Region;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.*;
import org.spoofax.jsglr.client.imploder.*;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.TokenExpectedException;

import java.util.*;

import static org.spoofax.jsglr.client.imploder.AbstractTokenizer.findLeftMostTokenOnSameLine;
import static org.spoofax.jsglr.client.imploder.AbstractTokenizer.findRightMostTokenOnSameLine;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getTokenizer;

public class ParserErrorHandler {
    private static final int LARGE_REGION_SIZE = 8;
    private static final String LARGE_REGION_START =
        "Region could not be parsed because of subsequent syntax error(s) indicated below";

    private final boolean recoveryEnabled;
    private final boolean recoveryFailed;
    private final Set<BadTokenException> parseErrors;

    private final ArrayList<Message> messages = new ArrayList<>();


    public ParserErrorHandler(boolean recoveryEnabled, boolean recoveryFailed, Set<BadTokenException> parseErrors) {
        this.recoveryEnabled = recoveryEnabled;
        this.recoveryFailed = recoveryFailed;
        this.parseErrors = parseErrors;
    }


    public ArrayList<Message> messages() {
        return messages;
    }


    /*
     * Non-fatal (recoverable) errors
     */

    public void gatherNonFatalErrors(IStrategoTerm top) {
        final ITokens tokens = getTokenizer(top);
        if(tokens != null) {
            for(int i = 0, max = tokens.getTokenCount(); i < max; i++) {
                final IToken token = tokens.getTokenAt(i);
                final String error = token.getError();
                if(error != null) {
                    if(error == ITokens.ERROR_SKIPPED_REGION) {
                        i = findRightMostWithSameError(token, null);
                        reportSkippedRegion(token, tokens.getTokenAt(i));
                    } else if(error.startsWith(ITokens.ERROR_WARNING_PREFIX)) {
                        i = findRightMostWithSameError(token, null);
                        reportWarningAtTokens(token, tokens.getTokenAt(i), error);
                    } else if(error.startsWith(ITokens.ERROR_WATER_PREFIX)) {
                        i = findRightMostWithSameError(token, ITokens.ERROR_WATER_PREFIX);
                        reportErrorAtTokens(token, tokens.getTokenAt(i), error);
                    } else {
                        i = findRightMostWithSameError(token, null);
                        // UNDONE: won't work for multi-token errors (as seen in
                        // SugarJ)
                        reportErrorAtTokens(token, tokens.getTokenAt(i), error);
                    }
                }
            }
        }
    }

    private static int findRightMostWithSameError(IToken token, String prefix) {
        final String expectedError = token.getError();
        final ITokens tokens = token.getTokenizer();
        int i = token.getIndex();
        for(int max = tokens.getTokenCount(); i + 1 < max; i++) {
            String error = tokens.getTokenAt(i + 1).getError();
            if(error != expectedError && (error == null || prefix == null || !error.startsWith(prefix)))
                break;
        }
        return i;
    }

    private void reportSkippedRegion(IToken left, IToken right) {
        // Find a parse failure(s) in the given token range
        int line = left.getLine();
        int reportedLine = -1;
        for(BadTokenException e : getCollectedErrorsInRegion(left, right, true)) {
            processFatalException(left.getTokenizer(), e);
            if(reportedLine == -1)
                reportedLine = e.getLineNumber();
        }

        if(reportedLine == -1) {
            // Report entire region
            reportErrorAtTokens(left, right, ITokens.ERROR_SKIPPED_REGION);
        } else if(reportedLine - line >= LARGE_REGION_SIZE) {
            // Warn at start of region
            reportErrorAtTokens(findLeftMostTokenOnSameLine(left), findRightMostTokenOnSameLine(left),
                LARGE_REGION_START);
        }
    }

    private List<BadTokenException> getCollectedErrorsInRegion(IToken left, IToken right, boolean alsoOutside) {
        final List<BadTokenException> results = new ArrayList<>();
        final int line = left.getLine();
        final int endLine = right.getLine() + (alsoOutside ? RegionRecovery.NR_OF_LINES_TILL_SUCCESS : 0);
        for(BadTokenException error : parseErrors) {
            if(error.getLineNumber() >= line && error.getLineNumber() <= endLine) {
                results.add(error);
            }
        }
        return results;
    }


    /*
     * Fatal errors
     */

    public void processFatalException(ITokens tokens, Exception exception) {
        try {
            throw exception;
        } catch(ParseTimeoutException e) {
            reportTimeOut(tokens, e);
        } catch(TokenExpectedException e) {
            reportTokenExpected(tokens, e);
        } catch(MultiBadTokenException e) {
            reportMultiBadToken(tokens, e);
        } catch(BadTokenException e) {
            reportBadToken(tokens, e);
        } catch(Exception e) {
            createErrorAtFirstLine("Internal parsing error: " + e);
        }
    }

    private void reportTimeOut(ITokens tokens, ParseTimeoutException exception) {
        final String message = "Internal parsing error: " + exception.getMessage();
        createErrorAtFirstLine(message);
        reportMultiBadToken(tokens, exception);
    }

    private void reportTokenExpected(ITokens tokens, TokenExpectedException exception) {
        final String message = exception.getShortMessage();
        reportErrorNearOffset(tokens, exception.getOffset(), message);
    }

    private void reportMultiBadToken(ITokens tokens, MultiBadTokenException exception) {
        for(BadTokenException e : exception.getCauses()) {
            processFatalException(tokens, e);
        }
    }

    private void reportBadToken(ITokens tokens, BadTokenException exception) {
        final String message;
        if(exception.isEOFToken() || tokens.getTokenCount() <= 1) {
            message = exception.getShortMessage();
        } else {
            IToken token = tokens.getTokenAtOffset(exception.getOffset());
            token = findNextNonEmptyToken(token);
            message = ITokens.ERROR_WATER_PREFIX + ": " + token.toString().trim();
        }
        reportErrorNearOffset(tokens, exception.getOffset(), message);
    }


    private void reportErrorNearOffset(ITokens tokens, int offset, String message) {
        final IToken errorToken = ((AbstractTokenizer) tokens).getErrorTokenOrAdjunct(offset);
        final Region region = RegionFactory.fromTokens(errorToken, errorToken);
        reportErrorAtRegion(region, message);
    }

    private static IToken findNextNonEmptyToken(IToken token) {
        final ITokens tokens = token.getTokenizer();
        IToken result = null;
        for(int i = token.getIndex(), max = tokens.getTokenCount(); i < max; i++) {
            result = tokens.getTokenAt(i);
            if(result.getLength() != 0 && !Token.isWhiteSpace(result))
                break;
        }
        return result;
    }


    private void createErrorAtFirstLine(String text) {
        final String errorText = text + getErrorExplanation();
        final Message message = new Message(errorText, Severity.Error);
        messages.add(message);
    }

    private void reportErrorAtTokens(IToken left, IToken right, String text) {
        final Region region = RegionFactory.fromTokens(left, right);
        reportErrorAtRegion(region, text);
    }

    private void reportErrorAtRegion(Region region, String text) {
        final Message message = new Message(text, Severity.Error, region);
        messages.add(message);
    }

    private void reportWarningAtTokens(IToken left, IToken right, String text) {
        reportWarningAtRegion(RegionFactory.fromTokens(left, right), text);
    }

    private void reportWarningAtRegion(Region region, String text) {
        final Message message = new Message(text, Severity.Warn, region);
        messages.add(message);
    }


    private String getErrorExplanation() {
        if(recoveryFailed) {
            return " (recovery failed)";
        } else if(!recoveryEnabled) {
            return " (recovery disabled)";
        } else {
            return "";
        }
    }
}
