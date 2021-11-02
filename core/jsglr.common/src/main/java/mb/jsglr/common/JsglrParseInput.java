package mb.jsglr.common;

import mb.common.text.Text;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * JSGLR parser input.
 */
public final class JsglrParseInput implements Serializable {

    public final Text text;
    public final String startSymbol;
    public final @Nullable ResourceKey fileHint;
    public final @Nullable ResourcePath rootDirectoryHint;
    public final boolean codeCompletionMode;
    public final int cursorOffset;

    /**
     * Initializes a new instance of the {@link JsglrParseInput} class.
     *
     * @param text the text to be parsed
     * @param startSymbol the start symbol of the grammar
     * @param fileHint a hint of the file being parsed; or {@code null} when not specified
     * @param rootDirectoryHint a hint of the project root directory; or {@code null} when not specified
     * @param codeCompletionMode whether to parse in completion mode
     * @param cursorOffset the zero-based cursor offset in the text
     */
    public JsglrParseInput(
        Text text,
        String startSymbol,
        @Nullable ResourceKey fileHint,
        @Nullable ResourcePath rootDirectoryHint,
        boolean codeCompletionMode,
        int cursorOffset
    ) {
        this.text = text;
        this.startSymbol = startSymbol;
        this.fileHint = fileHint;
        this.rootDirectoryHint = rootDirectoryHint;
        this.codeCompletionMode = codeCompletionMode;
        this.cursorOffset = cursorOffset;
    }

    public JsglrParseInput(
        Text text,
        String startSymbol,
        @Nullable ResourceKey fileHint,
        @Nullable ResourcePath rootDirectoryHint
    ) {
        this(text, startSymbol, fileHint, rootDirectoryHint, false, 0);
    }

    public JsglrParseInput(Text text, String startSymbol, @Nullable ResourceKey fileHint) {
        this(text, startSymbol, fileHint, null);
    }

    public JsglrParseInput(Text text, String startSymbol) {
        this(text, startSymbol, null);
    }

    public JsglrParseInput(String text, String startSymbol, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        this(Text.string(text), startSymbol, fileHint, rootDirectoryHint);
    }

    public JsglrParseInput(String text, String startSymbol, @Nullable ResourceKey fileHint) {
        this(Text.string(text), startSymbol, fileHint);
    }

    public JsglrParseInput(String text, String startSymbol) {
        this(Text.string(text), startSymbol);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final JsglrParseInput that = (JsglrParseInput)o;
        return this.text.equals(that.text)
            && this.startSymbol.equals(that.startSymbol)
            && Objects.equals(this.fileHint, that.fileHint)
            && Objects.equals(this.rootDirectoryHint, that.rootDirectoryHint)
            && this.codeCompletionMode == that.codeCompletionMode
            && this.cursorOffset == that.cursorOffset;
    }

    @Override public int hashCode() {
        return Objects.hash(
            text,
            startSymbol,
            fileHint,
            rootDirectoryHint,
            codeCompletionMode,
            cursorOffset
        );
    }

    @Override public String toString() {
        return "JsglrParseInput{" +
            "text='" + text + '\'' +
            ", startSymbol='" + startSymbol + '\'' +
            ", fileHint=" + fileHint +
            ", rootDirectoryHint=" + rootDirectoryHint +
            ", codeCompletionMode=" + codeCompletionMode +
            ", cursorOffset=" + cursorOffset +
            '}';
    }
}
