package mb.sdf3.spoofax.task;

import mb.common.util.ListView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
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
        private final boolean createCompletionTable;

        public Args(
            Supplier<@Nullable IStrategoTerm> mainModuleAstSupplier,
            ListView<Supplier<@Nullable IStrategoTerm>> modulesAstSuppliers,
            ParseTableConfiguration parseTableConfiguration,
            boolean createCompletionTable
        ) {
            this.mainModuleAstSupplier = mainModuleAstSupplier;
            this.modulesAstSuppliers = modulesAstSuppliers;
            this.parseTableConfiguration = parseTableConfiguration;
            this.createCompletionTable = createCompletionTable;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Args args = (Args)o;
            return createCompletionTable == args.createCompletionTable &&
                mainModuleAstSupplier.equals(args.mainModuleAstSupplier) &&
                modulesAstSuppliers.equals(args.modulesAstSuppliers) &&
                parseTableConfiguration.equals(args.parseTableConfiguration);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mainModuleAstSupplier, modulesAstSuppliers, parseTableConfiguration, createCompletionTable);
        }
    }

    private final Logger log;
    private final Sdf3ToPermissive toPermissive;
    private final Sdf3ToCompletion toCompletion;
    private final Sdf3ToNormalForm toNormalForm;

    @Inject
    public Sdf3ToTable(
        LoggerFactory loggerFactory,
        Sdf3ToPermissive toPermissive,
        Sdf3ToCompletion toCompletion,
        Sdf3ToNormalForm toNormalForm
    ) {
        this.log = loggerFactory.create(Sdf3ToTable.class);
        this.toPermissive = toPermissive;
        this.toCompletion = toCompletion;
        this.toNormalForm = toNormalForm;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public @Nullable ParseTable exec(ExecContext context, Args args) throws Exception {
        final @Nullable IStrategoTerm mainNormalizedGrammar = context.require(toNormalized(args.mainModuleAstSupplier));
        if(mainNormalizedGrammar == null) {
            throw new ExecException("Transforming SDF3 grammar of main module " + args.mainModuleAstSupplier + " to normal form returned a null AST");
        }
        log.info("Main: {}", mainNormalizedGrammar);

        final NormGrammarReader normGrammarReader = new NormGrammarReader();

        for(Supplier<@Nullable IStrategoTerm> astSupplier : args.modulesAstSuppliers) {
            final @Nullable IStrategoTerm normalizedGrammarTerm = context.require(toNormalized(astSupplier));
            if(normalizedGrammarTerm == null) {
                throw new ExecException("Transforming SDF3 grammar of " + astSupplier + " to normal form returned a null AST");
            }
            log.info("Other: {}", normalizedGrammarTerm);
            normGrammarReader.addModuleAst(normalizedGrammarTerm);
        }

        final NormGrammar normalizedGrammar;
        if(!args.createCompletionTable) {
            normalizedGrammar = normGrammarReader.readGrammar(mainNormalizedGrammar);
        } else {
            // Add main normalized grammar, instead of using it as the main module, since the completion version of the
            // main module is the actual main module in case of creating a completion parse table.
            normGrammarReader.addModuleAst(mainNormalizedGrammar);

            final @Nullable IStrategoTerm mainCompletionNormalizedGrammar = context.require(toCompletionNormalized(args.mainModuleAstSupplier));
            if(mainCompletionNormalizedGrammar == null) {
                throw new ExecException("Transforming SDF3 grammar of main module " + args.mainModuleAstSupplier + " to completion normal form returned a null AST");
            }
            log.info("Main completion: {}", mainCompletionNormalizedGrammar);

            for(Supplier<@Nullable IStrategoTerm> astSupplier : args.modulesAstSuppliers) {
                final @Nullable IStrategoTerm normalizedGrammarTerm = context.require(toCompletionNormalized(astSupplier));
                if(normalizedGrammarTerm == null) {
                    throw new ExecException("Transforming SDF3 grammar of " + astSupplier + " to completion normal form returned a null AST");
                }
                log.info("Other completion: {}", normalizedGrammarTerm);
                normGrammarReader.addModuleAst(normalizedGrammarTerm);
            }

            normalizedGrammar = normGrammarReader.readGrammar(mainCompletionNormalizedGrammar);
        }

        return new ParseTable(normalizedGrammar, args.parseTableConfiguration);
    }

    private Task<@Nullable IStrategoTerm> toNormalized(Supplier<@Nullable IStrategoTerm> astSupplier) {
        return toNormalForm.createTask(toPermissive.createSupplier(astSupplier));
    }

    private Task<@Nullable IStrategoTerm> toCompletionNormalized(Supplier<@Nullable IStrategoTerm> astSupplier) {
        return toNormalForm.createTask(toCompletion.createSupplier(astSupplier));
    }
}
