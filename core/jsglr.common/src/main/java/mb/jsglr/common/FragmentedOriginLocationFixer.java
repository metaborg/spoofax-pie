package mb.jsglr.common;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.region.Region;
import mb.common.text.StringFragment;
import mb.common.text.Text;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokens;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.ListImploderAttachment;
import org.spoofax.jsglr.client.imploder.Token;
import org.spoofax.jsglr.client.imploder.Tokenizer;
import org.spoofax.terms.util.TermUtils;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

// Selectively copied from: org.metaborg.spt.core.run.SpoofaxOriginFragmentParser
public class FragmentedOriginLocationFixer {
    public static class Result {
        public final IStrategoTerm ast;
        public final ITokens tokens;
        public final KeyedMessages messages;

        public Result(IStrategoTerm ast, ITokens tokens, KeyedMessages messages) {
            this.ast = ast;
            this.tokens = tokens;
            this.messages = messages;
        }
    }

    public static Result fixOriginLocations(Text text, IStrategoTerm ast, ITokens tokens, KeyedMessages messages) {
        return text.caseOf().string(s -> new Result(ast, tokens, messages)).fragmentedString(fragmentedString -> {
            // Adjust the tokens for each piece of the fragment. This makes NO assumptions about the order of the
            // startOffsets of the token stream. It DOES assume that the pieces of text of the fragment are ordered based on
            // the correct order of text.
            final LinkedHashMap<IToken, Integer> startOffsets = new LinkedHashMap<>(tokens.getTokenCount());
            final LinkedHashMap<IToken, Integer> endOffsets = new LinkedHashMap<>(tokens.getTokenCount());
            @Nullable IToken eof = null;
            int currStartOffsetOfPiece = 0;
            int currEndOffsetOfPiece = 0;
            for(StringFragment stringFragment : fragmentedString.fragments) {
                int pieceLength = stringFragment.text.length();
                currEndOffsetOfPiece = currStartOffsetOfPiece + pieceLength - 1;
                int adjustment = stringFragment.startOffset - currStartOffsetOfPiece;
                for(IToken token : tokens.allTokens()) {
                    int startOffset = token.getStartOffset();
                    if(startOffset >= currStartOffsetOfPiece && startOffset <= currEndOffsetOfPiece) {
                        startOffsets.put(token, startOffset + adjustment);
                        endOffsets.put(token, token.getEndOffset() + adjustment);
                    }
                    if(token.getKind() == IToken.Kind.TK_EOF) {
                        eof = token;
                    }
                }
                currStartOffsetOfPiece += pieceLength;
            }

            // Only post process tokens when there are tokens, and when there is an end-of-file token.
            final @Nullable MappingTokens newTokens;
            if(!startOffsets.isEmpty() && eof != null) {
                newTokens = new MappingTokens(tokens);
                for(IToken token : tokens.allTokens()) {
                    if(token.getKind() == IToken.Kind.TK_EOF) {
                        int lastOffset = newTokens.tokens.get(newTokens.tokens.size() - 1).getEndOffset();
                        newTokens.addToken(lastOffset + 1, lastOffset, eof);
                    } else {
                        newTokens.addToken(
                            startOffsets.containsKey(token) ? startOffsets.get(token) : token.getStartOffset(),
                            endOffsets.containsKey(token) ? endOffsets.get(token) : token.getEndOffset(),
                            token
                        );
                    }
                }
                newTokens.overwriteAttachments(ast);
            } else {
                newTokens = null;
            }

            // Now the offsets of the tokens are updated. Changing the state like this should update the offsets of the ast
            // nodes automatically but next, we need to update the offsets of the parse messages.
            final KeyedMessages newMessages = messages.map(message -> {
                final @Nullable Region region = message.region;
                if(region == null) return message;
                int newStart = region.getStartOffset();
                int newEnd = region.getEndOffset();
                int offset = 0;
                for(StringFragment stringFragment : fragmentedString.fragments) {
                    int startOffset = region.getStartOffset();
                    int pieceEndExclusive = offset + stringFragment.text.length();
                    if(startOffset >= offset && startOffset < pieceEndExclusive) {
                        newStart = stringFragment.startOffset + (startOffset - offset);
                    }
                    int endOffset = region.getEndOffset();
                    if(endOffset >= offset && endOffset <= pieceEndExclusive) {
                        newEnd = stringFragment.startOffset + (endOffset - offset);
                    }
                    offset += stringFragment.text.length();
                }
                if(newStart != region.getStartOffset() || newEnd != region.getEndOffset()) {
                    return new Message(message.text, message.exception, message.severity, Region.fromOffsets(newStart, newEnd));
                }
                return message; // TODO: message is unchanged, is that ok? Spoofax 2 implementation discards the message.
            });

            return new Result(ast, newTokens != null ? newTokens : tokens, newMessages);
        });
    }

    private static class MappingTokens implements ITokens {
        private final ArrayList<IToken> tokens = new ArrayList<>();
        private final LinkedHashMap<IToken, IToken> oldToNewTokens = new LinkedHashMap<>();
        private final LinkedHashMap<IToken, IToken> newToOldTokens = new LinkedHashMap<>();
        private final String input;
        private final String filename;

        private MappingTokens(ITokens originalTokens) {
            this.input = originalTokens.getInput();
            this.filename = originalTokens.getFilename();
        }

        private void addToken(int startOffset, int endOffset, IToken originalToken) {
            Token newToken = new MappedToken(this, startOffset, endOffset, originalToken);
            newToken.setAstNode(originalToken.getAstNode());
            tokens.add(newToken);
            oldToNewTokens.put(originalToken, newToken);
            newToOldTokens.put(newToken, originalToken);
        }

        private void overwriteAttachments(IStrategoTerm ast) {
            StrategoTermVisitee.topdown(new AStrategoTermVisitor() {
                @Override public boolean visit(IStrategoTerm term) {
                    updateImploderAttachment(term);
                    if(TermUtils.isList(term)) {
                        IStrategoList sublist = TermUtils.toList(term);
                        while(!sublist.isEmpty()) {
                            sublist = sublist.tail();
                            updateImploderAttachment(sublist);
                        }
                    }
                    return true;
                }
            }, ast);
        }

        private void updateImploderAttachment(IStrategoTerm term) {
            ImploderAttachment originalAttachment = ImploderAttachment.get(term);

            // For incremental parsing, the reused AST nodes already have updated ImploderAttachments with new
            // MappedTokens. In this case, we should get the original token to index the oldToNewTokens Map,
            // because the offsets might be updated since the previous version.
            IToken leftToken = oldToNewTokens.get(originalAttachment.getLeftToken() instanceof MappedToken
                ? ((MappedToken)originalAttachment.getLeftToken()).originalToken : originalAttachment.getLeftToken());
            IToken rightToken = oldToNewTokens.get(originalAttachment.getRightToken() instanceof MappedToken
                ? ((MappedToken)originalAttachment.getRightToken()).originalToken
                : originalAttachment.getRightToken());

            ImploderAttachment.putImploderAttachment(term, term instanceof ListImploderAttachment,
                originalAttachment.getSort(), leftToken, rightToken, originalAttachment.isBracket(),
                originalAttachment.isCompletion(), originalAttachment.isNestedCompletion(),
                originalAttachment.isSinglePlaceholderCompletion());

            ImploderAttachment newAttachment = ImploderAttachment.get(term);
            originalAttachment.getInjections().forEach(newAttachment::pushInjection);
        }

        @Override public String getInput() {
            return input;
        }

        @Override public int getTokenCount() {
            return tokens.size();
        }

        @Override public IToken getTokenAtOffset(int offset) {
            for(IToken token : tokens) {
                if(token.getStartOffset() == offset)
                    return token;
            }
            return null;
        }

        @Override public String getFilename() {
            return filename;
        }

        @Override public String toString(IToken left, IToken right) {
            return toString(newToOldTokens.get(left).getStartOffset(), newToOldTokens.get(right).getEndOffset());
        }

        /**
         * @param endOffset The end offset is inclusive.
         */
        @Override public String toString(int startOffset, int endOffset) {
            return input.substring(startOffset, endOffset + 1);
        }

        @Override public Iterator<IToken> iterator() {
            return new Tokenizer.FilteredTokenIterator(allTokens());
        }

        @Override public Iterable<IToken> allTokens() {
            return Collections.unmodifiableList(tokens);
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final MappingTokens iTokens = (MappingTokens)o;
            if(!tokens.equals(iTokens.tokens)) return false;
            if(!oldToNewTokens.equals(iTokens.oldToNewTokens)) return false;
            if(!newToOldTokens.equals(iTokens.newToOldTokens)) return false;
            if(!input.equals(iTokens.input)) return false;
            return filename.equals(iTokens.filename);
        }

        @Override public int hashCode() {
            int result = tokens.hashCode();
            result = 31 * result + oldToNewTokens.hashCode();
            result = 31 * result + newToOldTokens.hashCode();
            result = 31 * result + input.hashCode();
            result = 31 * result + filename.hashCode();
            return result;
        }

        @Override public String toString() {
            return "MappingTokenizer{" +
                "tokens=" + tokens +
                ", oldToNewTokens=" + oldToNewTokens +
                ", newToOldTokens=" + newToOldTokens +
                ", input='" + input + '\'' +
                ", filename='" + filename + '\'' +
                '}';
        }
    }

    private static class MappedToken extends Token {
        private final IToken originalToken;

        public MappedToken(ITokens tokens, int startOffset, int endOffset, IToken originalToken) {
            super(tokens, tokens.getFilename(), -1, -1, -1, startOffset, endOffset, originalToken.getKind());
            this.originalToken = originalToken;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            if(!super.equals(o)) return false;
            final MappedToken that = (MappedToken)o;
            return originalToken.equals(that.originalToken);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + originalToken.hashCode();
            return result;
        }

        @Override public String toString() {
            return "MappedToken{" +
                "originalToken=" + originalToken +
                '}';
        }
    }
}
