package mb.jsglr.common;

import mb.common.text.Text;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class JsglrParseInput implements Serializable {
    public final Text text;
    public final String startSymbol;
    public final @Nullable ResourceKey fileHint;
    public final @Nullable ResourcePath rootDirectoryHint;

    public JsglrParseInput(Text text, String startSymbol, @Nullable ResourceKey fileHint, @Nullable ResourcePath rootDirectoryHint) {
        this.text = text;
        this.startSymbol = startSymbol;
        this.fileHint = fileHint;
        this.rootDirectoryHint = rootDirectoryHint;
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
        if(!text.equals(that.text)) return false;
        if(!startSymbol.equals(that.startSymbol)) return false;
        if(fileHint != null ? !fileHint.equals(that.fileHint) : that.fileHint != null) return false;
        return rootDirectoryHint != null ? rootDirectoryHint.equals(that.rootDirectoryHint) : that.rootDirectoryHint == null;
    }

    @Override public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + startSymbol.hashCode();
        result = 31 * result + (fileHint != null ? fileHint.hashCode() : 0);
        result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "JsglrParseInput{" +
            "text='" + text + '\'' +
            ", startSymbol='" + startSymbol + '\'' +
            ", fileHint=" + fileHint +
            ", rootDirectoryHint=" + rootDirectoryHint +
            '}';
    }
}
