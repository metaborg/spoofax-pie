package mb.sdf3.spoofax.task;

import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import org.metaborg.sdf2parenthesize.parenthesizer.Parenthesizer;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

@LanguageScope
public class Sdf3SpecToParenthesizer implements TaskDef<Sdf3SpecToParenthesizer.Args, IStrategoTerm> {
    public static class Args implements Serializable {
        private final Supplier<ParseTable> parseTableSupplier;
        private final String moduleName;

        public Args(Supplier<ParseTable> parseTableSupplier, String moduleName) {
            this.parseTableSupplier = parseTableSupplier;
            this.moduleName = moduleName;
        }

        @Override public boolean equals(Object o) {
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
    }

    @Inject public Sdf3SpecToParenthesizer() {

    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public IStrategoTerm exec(ExecContext context, Args args) throws Exception {
        final ParseTable parseTable = context.require(args.parseTableSupplier);
        return Parenthesizer.generateParenthesizer(args.moduleName, null, parseTable);
    }
}
