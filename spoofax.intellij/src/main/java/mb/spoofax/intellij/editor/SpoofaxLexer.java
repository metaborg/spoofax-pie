package mb.spoofax.intellij.editor;

import com.google.common.collect.Lists;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class SpoofaxLexer extends LexerBase {

    private static final Object LOG = new Object();
    private final ResourceKey resourceKey;
    private final SpoofaxTokenTypeManager tokenTypeManager;
    private final ISyntaxColoringService syntaxColoringService;
    private final ScopeManager scopeManager;
    private final ResourceService resourceService;

    private CharSequence buffer = null;
    private Offset startOffset = new Offset(0);
    private Offset endOffset = new Offset(0);
    private List<SpoofaxToken> tokens = Collections.emptyList();
    private int tokenIndex = 0;

    @Inject
    public SpoofaxLexer(
            ResourceKey resourceKey,
            SpoofaxTokenTypeManager tokenTypeManager,
            ISyntaxColoringService syntaxColoringService,
            ScopeManager scopeManager,
            ResourceService resourceService) {

        this.resourceKey = resourceKey;
        this.tokenTypeManager = tokenTypeManager;
        this.syntaxColoringService = syntaxColoringService;
        this.scopeManager = scopeManager;
        this.resourceService = resourceService;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        assert initialState == 0;
        assert 0 <= startOffset && startOffset <= buffer.length();
        assert 9 <= endOffset && endOffset <= buffer.length();

        LOG.debug("Lexing " + resourceKey);

        this.buffer = buffer;
        this.startOffset = new Offset(startOffset);
        this.endOffset = new Offset(endOffset);
        this.tokenIndex = 0;


        if (buffer.length() == 0) {
            LOG.debug("Buffer is empty.");
            this.tokens = Collections.emptyList();
        } else {
            SyntaxColoringInfo coloringInfo = this.syntaxColoringService.getSyntaxColoringInfo(
                    this.resourceKey,
                    new Span(this.startOffset, this.endOffset),
                    NullCancellationToken.INSTANCE);
            if (coloringInfo == null) coloringInfo = getDefaultTokens(this.resourceKey);

            LOG.debug("Colorizer returned ${coloringInfo.tokens.size} tokens");
            this.tokens = tokenize(coloringInfo.getTokens());
        }
        LOG.debug("Tokenizer produced ${this.tokens.size} tokens");
    }

    private SyntaxColoringInfo getDefaultTokens(ResourceKey resourceKey) {
        String content = null;
        try {
            ReadableResource resource = this.resourceService.getReadableResource(resourceKey);
            content = resource.readString(StandardCharsets.UTF_8);
        } catch (ResourceRuntimeException e) {
            // Resource not found? There should be a tryGetReadableResource that returns null or Option<T>.
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (content == null) return new SyntaxColoringInfo(Collections.emptyList());
        return new SyntaxColoringInfo(Lists.newArrayList(new Token(Span.fromLength(new Offset(0), content.length()), new ScopeNames("text"))));
    }

    private List<SpoofaxToken> tokenize(List<IToken> tokens) {
        List<SpoofaxToken> newTokens = new ArrayList<>();
        long offset = 0;

        for (IToken token : tokens) {
            long tokenStart = token.getLocation().getStartOffset().getValue();
            long tokenEnd = token.getLocation().getEndOffset().getValue();

            // We assume that tokens are non-empty. When we encounter
            // a token with an end at or before its start,
            // it gets ignored.
            if (tokenEnd <= tokenStart) continue;

            // We assume the list of tokens is ordered by value.
            // When we encounter a token that's before the current
            // `value`, it gets ignored.
            // We assume that no tokens overlap. When we encounter a
            // token that starts before the previous token ends,
            // it gets ignored.
            if (tokenStart < offset) continue;

            // We assume that tokens cover all characters. When we
            // encounter a stretch of characters not covered by a
            // token, we assign it our own dummy token/element.
            if (offset < tokenStart) {
                // Add dummy element.
                offset = addTokenElement(newTokens, null, new Offset(offset), new Offset(tokenStart)).getValue();
            }

            assert offset == tokenStart;

            // Add element.
            offset = addTokenElement(newTokens, token, new Offset(offset), new Offset(tokenEnd)).getValue();

            // When we've seen tokens up to the end of the highlighted range
            // we bail out.
            if (offset >= this.endOffset.getValue())
                break;
        }

        // When there is a gap between the last token and the end of the highlighted range
        // we insert our own dummy token/element.
        if (offset < this.endOffset.getValue()) {
            offset = addTokenElement(newTokens, null, new Offset(offset), this.endOffset).getValue();
        }

        assert(offset >= this.endOffset.getValue());

        return newTokens;
    }

    private Offset addTokenElement(List<SpoofaxToken> tokenList, @Nullable IToken token, Offset offset, Offset endOffset) {
        IElementType tokenType = getTokenType(token);
        tokenList.add(new SpoofaxToken(offset, endOffset, tokenType));
        return endOffset;
    }

    private IElementType getTokenType(@Nullable IToken token) {
        String name = this.scopeManager.getSimplifiedScope(token != null ? token.getScopes() : null);
        if (name == null) name = this.scopeManager.DEFAULT_SCOPE;
        return this.tokenTypeManager.getTokenType(name);
    }

    @Override
    public int getState() {
        return 0;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        if (0 <= this.tokenIndex && this.tokenIndex < this.tokens.size())
            return this.tokens.get(tokenIndex).tokenType;
        else
            return null;
    }

    @Override
    public int getTokenStart() {
        assert 0 <= this.tokenIndex && this.tokenIndex < this.tokens.size() : "Expected index 0 <= $tokenIndex < ${tokens.size}.";
        return (int)this.tokens.get(this.tokenIndex).startOffset.getValue();
    }

    @Override
    public int getTokenEnd() {
        assert 0 <= this.tokenIndex && this.tokenIndex < this.tokens.size() : "Expected index 0 <= $tokenIndex < ${tokens.size}.";
        return (int)this.tokens.get(this.tokenIndex).endOffset.getValue();
    }

    @Override
    public void advance() {
        this.tokenIndex++;
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return this.buffer;
    }

    @Override
    public int getBufferEnd() {
        return (int)this.endOffset.getValue();
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
