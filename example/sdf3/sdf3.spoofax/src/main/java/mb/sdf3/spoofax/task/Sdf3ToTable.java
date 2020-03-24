package mb.sdf3.spoofax.task;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.io.NormGrammarReader;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

@LanguageScope
public class Sdf3ToTable implements TaskDef<Sdf3ToTable.Args, ParseTable> {
    public static class Args implements Serializable {
        private final Supplier<@Nullable IStrategoTerm> mainModuleAstSupplier;
        private final ArrayList<Supplier<@Nullable IStrategoTerm>> modulesAstSuppliers;
        private final ParseTableConfiguration parseTableConfiguration;

        public Args(
            Supplier<@Nullable IStrategoTerm> mainModuleAstSupplier,
            ArrayList<Supplier<@Nullable IStrategoTerm>> modulesAstSuppliers,
            ParseTableConfiguration parseTableConfiguration
        ) {
            this.mainModuleAstSupplier = mainModuleAstSupplier;
            this.modulesAstSuppliers = modulesAstSuppliers;
            this.parseTableConfiguration = parseTableConfiguration;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Args args = (Args)o;
            return mainModuleAstSupplier.equals(args.mainModuleAstSupplier) &&
                modulesAstSuppliers.equals(args.modulesAstSuppliers) &&
                parseTableConfiguration.equals(args.parseTableConfiguration);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mainModuleAstSupplier, modulesAstSuppliers, parseTableConfiguration);
        }
    }

    private final Sdf3ToNormalForm toNormalForm;

    public Sdf3ToTable(Sdf3ToNormalForm toNormalForm) {
        this.toNormalForm = toNormalForm;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable ParseTable exec(ExecContext context, Args args) throws Exception {
        final @Nullable IStrategoTerm mainNormalizedGrammar = context.require(toNormalForm.createTask(args.mainModuleAstSupplier));
        if(mainNormalizedGrammar == null) {
            throw new ExecException("Transforming SDF3 grammar of main module " + args.mainModuleAstSupplier + " to normal form returned a null AST");
        }

        final ArrayList<IStrategoTerm> normalizedGrammars = new ArrayList<>(args.modulesAstSuppliers.size());
        for(Supplier<@Nullable IStrategoTerm> astSupplier : args.modulesAstSuppliers) {
            final @Nullable IStrategoTerm normalizedGrammar = context.require(toNormalForm.createTask(astSupplier));
            if(normalizedGrammar == null) {
                throw new ExecException("Transforming SDF3 grammar of " + astSupplier + " to normal form returned a null AST");
            }
            normalizedGrammars.add(normalizedGrammar);
        }

        final NormGrammarReader normGrammarReader = new NormGrammarReader();
        // TODO: feed normalized grammar ASTs into normGrammarReader.
        final NormGrammar normalizedGrammar = normGrammarReader.readGrammar(mainNormalizedGrammar);

        return new ParseTable(normalizedGrammar, args.parseTableConfiguration);
    }
}
