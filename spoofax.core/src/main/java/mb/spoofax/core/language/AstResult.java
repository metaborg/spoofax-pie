package mb.spoofax.core.language;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.Objects;

public class AstResult implements Serializable {
    public final @Nullable IStrategoTerm ast;
    public final boolean recovered;

    public AstResult(@Nullable IStrategoTerm ast, boolean recovered) {
        this.ast = ast;
        this.recovered = recovered;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final AstResult astResult = (AstResult) o;
        if(recovered != astResult.recovered) return false;
        return Objects.equals(ast, astResult.ast);

    }

    @Override public int hashCode() {
        int result = ast != null ? ast.hashCode() : 0;
        result = 31 * result + (recovered ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "AstResult{" +
            "ast=" + ast +
            ", recovered=" + recovered +
            '}';
    }
}
