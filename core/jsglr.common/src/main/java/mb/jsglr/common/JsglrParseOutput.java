package mb.jsglr.common;

import mb.common.message.KeyedMessages;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;

public class JsglrParseOutput implements Serializable {
    public final IStrategoTerm ast;
    public final JSGLRTokens tokens;
    public final KeyedMessages messages;
    public final boolean recovered;
    public final boolean ambiguous;
    public final String startSymbol;
    public final @Nullable ResourceKey fileHint;
    public final @Nullable ResourcePath rootDirectoryHint;

    public JsglrParseOutput(
        IStrategoTerm ast,
        JSGLRTokens tokens,
        KeyedMessages messages,
        boolean recovered,
        boolean ambiguous,
        String startSymbol,
        @Nullable ResourceKey fileHint,
        @Nullable ResourcePath rootDirectoryHint
    ) {
        this.ast = ast;
        this.tokens = tokens;
        this.messages = messages;
        this.recovered = recovered;
        this.ambiguous = ambiguous;
        this.startSymbol = startSymbol;
        this.fileHint = fileHint;
        this.rootDirectoryHint = rootDirectoryHint;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final JsglrParseOutput that = (JsglrParseOutput)o;
        if(recovered != that.recovered) return false;
        if(ambiguous != that.ambiguous) return false;
        if(!ast.equals(that.ast)) return false;
        if(!tokens.equals(that.tokens)) return false;
        if(!messages.equals(that.messages)) return false;
        if(!startSymbol.equals(that.startSymbol)) return false;
        if(fileHint != null ? !fileHint.equals(that.fileHint) : that.fileHint != null) return false;
        return rootDirectoryHint != null ? rootDirectoryHint.equals(that.rootDirectoryHint) : that.rootDirectoryHint == null;
    }

    @Override public int hashCode() {
        int result = ast.hashCode();
        result = 31 * result + tokens.hashCode();
        result = 31 * result + messages.hashCode();
        result = 31 * result + (recovered ? 1 : 0);
        result = 31 * result + (ambiguous ? 1 : 0);
        result = 31 * result + startSymbol.hashCode();
        result = 31 * result + (fileHint != null ? fileHint.hashCode() : 0);
        result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "JsglrParseOutput{" +
            "ast=" + ast +
            ", tokens=" + tokens +
            ", messages=" + messages +
            ", recovered=" + recovered +
            ", ambiguous=" + ambiguous +
            ", startSymbol='" + startSymbol + '\'' +
            ", fileHint=" + fileHint +
            ", rootDirectoryHint=" + rootDirectoryHint +
            '}';
    }
}
