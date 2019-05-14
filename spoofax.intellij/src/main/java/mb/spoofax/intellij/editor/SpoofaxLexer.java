package mb.spoofax.intellij.editor;

import com.google.common.collect.Lists;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import mb.common.style.Styling;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.api.PieSession;
import mb.pie.api.Task;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.intellij.Offset;
import mb.spoofax.intellij.ScopeNames;
import mb.spoofax.intellij.Span;
import mb.spoofax.intellij.psi.SpoofaxTokenTypeManager;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpoofaxLexer extends LexerBase {
    private final ResourceKey resourceKey;

    private final Logger logger;
    private final SpoofaxTokenTypeManager tokenTypeManager;
    private final ScopeManager scopeManager;
    private final ResourceService resourceService;
    private final Provider<PieSession> pieSessionProvider;
    private final LanguageInstance languageInstance;

    private CharSequence buffer = null;
    private Offset startOffset = new Offset(0); // GK: this field is not used?
    private Offset endOffset = new Offset(0);
    private List<SpoofaxToken> tokens = Collections.emptyList();
    private int tokenIndex = 0;


    public static class Factory {
        private final LoggerFactory loggerFactory;
        private final SpoofaxTokenTypeManager tokenTypeManager;
        private final ScopeManager scopeManager;
        private final ResourceService resourceService;
        private final Provider<PieSession> pieSessionProvider;
        private final LanguageInstance languageInstance;

        @Inject public Factory(
            LoggerFactory loggerFactory,
            SpoofaxTokenTypeManager tokenTypeManager,
            ScopeManager scopeManager,
            ResourceService resourceService,
            Provider<PieSession> pieSessionProvider,
            LanguageInstance languageInstance
        ) {
            this.loggerFactory = loggerFactory;
            this.tokenTypeManager = tokenTypeManager;
            this.scopeManager = scopeManager;
            this.resourceService = resourceService;
            this.pieSessionProvider = pieSessionProvider;
            this.languageInstance = languageInstance;
        }

        public SpoofaxLexer create(ResourceKey resourceKey) {
            return new SpoofaxLexer(
                    resourceKey,
                    loggerFactory,
                    tokenTypeManager,
                    scopeManager,
                    resourceService,
                    pieSessionProvider,
                    languageInstance);
        }
    }

    public SpoofaxLexer(
        ResourceKey resourceKey,
        LoggerFactory loggerFactory,
        SpoofaxTokenTypeManager tokenTypeManager,
        ScopeManager scopeManager,
        ResourceService resourceService,
        Provider<PieSession> pieSessionProvider,
        LanguageInstance languageInstance
    ) {
        this.resourceKey = resourceKey;
        this.logger = loggerFactory.create(getClass());
        this.tokenTypeManager = tokenTypeManager;
        this.scopeManager = scopeManager;
        this.resourceService = resourceService;
        this.pieSessionProvider = pieSessionProvider;
        this.languageInstance = languageInstance;
    }


    @Override public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        assert initialState == 0;
        assert 0 <= startOffset && startOffset <= buffer.length();
        assert 9 <= endOffset && endOffset <= buffer.length();

        logger.debug("Lexing " + this.resourceKey);

        this.buffer = buffer;
        this.startOffset = new Offset(startOffset);
        this.endOffset = new Offset(endOffset);
        this.tokenIndex = 0;


        if(buffer.length() == 0) {
            logger.debug("Buffer is empty.");
            this.tokens = Collections.emptyList();
        } else {
            // GK: what is syntax coloring information doing here?
            try(final PieSession session = this.pieSessionProvider.get()) {
                final Task<@Nullable Styling> stylingTask =
                    this.languageInstance.createStylingTask(this.resourceKey);
                final @Nullable Styling styling = session.requireTopDown(stylingTask);
                if(styling != null) {
                    // TODO: adapt to Daniel's code.
//                    SyntaxColoringInfo coloringInfo = this.syntaxColoringService.getSyntaxColoringInfo(
//                        this.resourceKey,
//                        new Span(this.startOffset, this.endOffset),
//                        NullCancellationToken.INSTANCE);
//                    if(coloringInfo == null) coloringInfo = getDefaultTokens(this.resourceKey);
//
//                    logger.debug("Colorizer returned {} tokens", coloringInfo.tokens.size());
//                    this.tokens = tokenize(coloringInfo.getTokens());
                }
            } catch(ExecException e) {
                throw new RuntimeException("Styling resource '" + this.resourceKey + "' failed unexpectedly", e);
            }
        }
        logger.debug("Tokenizer produced {} tokens", this.tokens.size());
    }

    private SyntaxColoringInfo getDefaultTokens(ResourceKey resourceKey) {
        final ReadableResource resource = this.resourceService.getReadableResource(resourceKey);
        try {
            final String content = resource.readString(StandardCharsets.UTF_8);
            return new SyntaxColoringInfo(Lists.newArrayList(
                new Token(Span.fromLength(new Offset(0), content.length()), new ScopeNames("text"))));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<SpoofaxToken> tokenize(List<IToken> tokens) {
        List<SpoofaxToken> newTokens = new ArrayList<>();
        long offset = 0;

        for(IToken token : tokens) {
            long tokenStart = token.getLocation().getStartOffset().getValue();
            long tokenEnd = token.getLocation().getEndOffset().getValue();

            // We assume that tokens are non-empty. When we encounter
            // a token with an end at or before its start,
            // it gets ignored.
            if(tokenEnd <= tokenStart) continue;

            // We assume the list of tokens is ordered by value.
            // When we encounter a token that's before the current
            // `value`, it gets ignored.
            // We assume that no tokens overlap. When we encounter a
            // token that starts before the previous token ends,
            // it gets ignored.
            if(tokenStart < offset) continue;

            // We assume that tokens cover all characters. When we
            // encounter a stretch of characters not covered by a
            // token, we assign it our own dummy token/element.
            if(offset < tokenStart) {
                // Add dummy element.
                offset = addTokenElement(newTokens, null, new Offset(offset), new Offset(tokenStart)).getValue();
            }

            assert offset == tokenStart;

            // Add element.
            offset = addTokenElement(newTokens, token, new Offset(offset), new Offset(tokenEnd)).getValue();

            // When we've seen tokens up to the end of the highlighted range
            // we bail out.
            if(offset >= this.endOffset.getValue())
                break;
        }

        // When there is a gap between the last token and the end of the highlighted range
        // we insert our own dummy token/element.
        if(offset < this.endOffset.getValue()) {
            offset = addTokenElement(newTokens, null, new Offset(offset), this.endOffset).getValue();
        }

        assert (offset >= this.endOffset.getValue());

        return newTokens;
    }

    private Offset addTokenElement(List<SpoofaxToken> tokenList,
        @Nullable IToken token, Offset offset, Offset endOffset) {
        IElementType tokenType = getTokenType(token);
        tokenList.add(new SpoofaxToken(offset, endOffset, tokenType));
        return endOffset;
    }

    private IElementType getTokenType(@Nullable IToken token) {
        final String name;
        if(token != null) {
            name = this.scopeManager.getSimplifiedScope(token.getScopes());
        } else {
            name = scopeManager.DEFAULT_SCOPE;
        }
        return tokenTypeManager.getTokenType(name);
    }

    @Override
    public int getState() {
        return 0;
    }


    @Override
    public @Nullable IElementType getTokenType() {
        if(0 <= this.tokenIndex && this.tokenIndex < this.tokens.size())
            return this.tokens.get(tokenIndex).tokenType;
        else
            return null;
    }

    @Override
    public int getTokenStart() {
        assert 0 <= this.tokenIndex && this.tokenIndex < this.tokens.size() : "Expected index 0 <= $tokenIndex < ${tokens.size}.";
        return (int) this.tokens.get(this.tokenIndex).startOffset.getValue();
    }

    @Override
    public int getTokenEnd() {
        assert 0 <= this.tokenIndex && this.tokenIndex < this.tokens.size() : "Expected index 0 <= $tokenIndex < ${tokens.size}.";
        return (int) this.tokens.get(this.tokenIndex).endOffset.getValue();
    }

    @Override
    public void advance() {
        this.tokenIndex++;
    }

    @Override
    public CharSequence getBufferSequence() {
        return this.buffer;
    }

    @Override
    public int getBufferEnd() {
        return (int) this.endOffset.getValue();
    }

    private static class SpoofaxToken {
        private final Offset startOffset;
        private final Offset endOffset;
        private IElementType tokenType;

        public SpoofaxToken(Offset startOffset, Offset endOffset, IElementType tokenType) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.tokenType = tokenType;
        }

        public Offset getStartOffset() {
            return startOffset;
        }

        public Offset getEndOffset() {
            return endOffset;
        }

        public IElementType getTokenType() {
            return tokenType;
        }
    }
}
