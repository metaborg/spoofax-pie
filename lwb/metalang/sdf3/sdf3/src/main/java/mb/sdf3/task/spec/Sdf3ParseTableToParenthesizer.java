package mb.sdf3.task.spec;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.sdf3.Sdf3Scope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2parenthesize.parenthesizer.Parenthesizer;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Sdf3Scope
public class Sdf3ParseTableToParenthesizer implements TaskDef<Sdf3ParseTableToParenthesizer.Args, Result<IStrategoTerm, ?>> {
    public static class Args implements Serializable {
        private final Supplier<? extends Result<ParseTable, ?>> parseTableSupplier;
        private final String moduleName;

        public Args(Supplier<? extends Result<ParseTable, ?>> parseTableSupplier, String moduleName) {
            this.parseTableSupplier = parseTableSupplier;
            this.moduleName = moduleName;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Args args = (Args)o;
            return parseTableSupplier.equals(args.parseTableSupplier) &&
                moduleName.equals(args.moduleName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parseTableSupplier, moduleName);
        }

        @Override public String toString() {
            return "Sdf3ParseTableToParenthesizer$Args{" +
                "parseTableSupplier=" + parseTableSupplier +
                ", moduleName='" + moduleName + '\'' +
                '}';
        }
    }

    @Inject public Sdf3ParseTableToParenthesizer() { /* Default @Inject constructor required */ }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<IStrategoTerm, ?> exec(ExecContext context, Args args) throws IOException {
        return context.require(args.parseTableSupplier)
            .map(pt -> Parenthesizer.generateParenthesizer(args.moduleName, null, pt));
    }

    @Override public boolean shouldExecWhenAffected(Args input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
