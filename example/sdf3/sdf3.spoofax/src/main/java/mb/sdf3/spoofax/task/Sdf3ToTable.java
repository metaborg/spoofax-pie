package mb.sdf3.spoofax.task;

import mb.common.util.ListView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
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

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

@LanguageScope
public class Sdf3ToTable implements TaskDef<Sdf3ToTable.Args, ParseTable> {
    public static class Args implements Serializable {
        private final Supplier<@Nullable IStrategoTerm> mainModuleAstSupplier;
        private final ListView<Supplier<@Nullable IStrategoTerm>> modulesAstSuppliers;
        private final ParseTableConfiguration parseTableConfiguration;

        public Args(
            Supplier<@Nullable IStrategoTerm> mainModuleAstSupplier,
            ListView<Supplier<@Nullable IStrategoTerm>> modulesAstSuppliers,
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

    private final Logger log;
    private final Sdf3ToPermissive toPermissive;
    private final Sdf3ToNormalForm toNormalForm;

    @Inject
    public Sdf3ToTable(LoggerFactory loggerFactory, Sdf3ToPermissive toPermissive, Sdf3ToNormalForm toNormalForm) {
        this.log = loggerFactory.create(Sdf3ToTable.class);
        this.toPermissive = toPermissive;
        this.toNormalForm = toNormalForm;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public @Nullable ParseTable exec(ExecContext context, Args args) throws Exception {
        final @Nullable IStrategoTerm mainNormalizedGrammar = context.require(toNormalForm.createTask(toPermissive.createSupplier(args.mainModuleAstSupplier)));
        if(mainNormalizedGrammar == null) {
            throw new ExecException("Transforming SDF3 grammar of main module " + args.mainModuleAstSupplier + " to normal form returned a null AST");
        }
        log.info("Main: {}", mainNormalizedGrammar);

        final NormGrammarReader normGrammarReader = new NormGrammarReader();
        for(Supplier<@Nullable IStrategoTerm> astSupplier : args.modulesAstSuppliers) {
            final @Nullable IStrategoTerm normalizedGrammar = context.require(toNormalForm.createTask(toPermissive.createSupplier(astSupplier)));
            if(normalizedGrammar == null) {
                throw new ExecException("Transforming SDF3 grammar of " + astSupplier + " to normal form returned a null AST");
            }
            log.info("Other: {}", normalizedGrammar);
            normGrammarReader.addModuleAst(normalizedGrammar);
        }

        final NormGrammar normalizedGrammar = normGrammarReader.readGrammar(mainNormalizedGrammar);
        log.info("Full normalized grammar: {}", normalizedGrammar);

        final ParseTable parseTable = new ParseTable(normalizedGrammar, args.parseTableConfiguration);
        log.info("Parse table: {}", parseTable);

        return parseTable;
    }
}
