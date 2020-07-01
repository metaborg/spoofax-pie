package mb.sdf3.spoofax.task;

import mb.common.result.Result;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.io.NormGrammarReader;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

@LanguageScope
public class Sdf3SpecToParseTable implements TaskDef<Sdf3SpecToParseTable.Args, Result<ParseTable, ?>> {
    public static class Args implements Serializable {
        private final Supplier<Sdf3Spec> specSupplier;
        private final ParseTableConfiguration parseTableConfiguration;
        private final boolean createCompletionTable;

        public Args(Supplier<Sdf3Spec> specSupplier, ParseTableConfiguration parseTableConfiguration, boolean createCompletionTable) {
            this.specSupplier = specSupplier;
            this.parseTableConfiguration = parseTableConfiguration;
            this.createCompletionTable = createCompletionTable;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return createCompletionTable == args.createCompletionTable &&
                specSupplier.equals(args.specSupplier) &&
                parseTableConfiguration.equals(args.parseTableConfiguration);
        }

        @Override public int hashCode() {
            return Objects.hash(specSupplier, parseTableConfiguration, createCompletionTable);
        }
    }

    private final Logger log;
    private final Sdf3ToPermissive toPermissive;
    private final Sdf3ToCompletion toCompletion;
    private final Sdf3ToNormalForm toNormalForm;

    @Inject public Sdf3SpecToParseTable(
        LoggerFactory loggerFactory,
        Sdf3ToPermissive toPermissive,
        Sdf3ToCompletion toCompletion,
        Sdf3ToNormalForm toNormalForm
    ) {
        this.log = loggerFactory.create(Sdf3SpecToParseTable.class);
        this.toPermissive = toPermissive;
        this.toCompletion = toCompletion;
        this.toNormalForm = toNormalForm;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<ParseTable, ?> exec(ExecContext context, Args args) throws IOException {
        final Sdf3Spec spec = context.require(args.specSupplier);

        final Result<IStrategoTerm, ?> mainNormalizedGrammarResult = context.require(toNormalized(spec.mainModuleAstSupplier));
        if(mainNormalizedGrammarResult.isErr()) {
            // TODO: nicer error pattern?
            return Result.ofErr(new Exception("Transforming SDF3 grammar of main module " + spec.mainModuleAstSupplier + " to normal form failed", mainNormalizedGrammarResult.getErr()));
        }
        final IStrategoTerm mainNormalizedGrammar = mainNormalizedGrammarResult.get();
        log.info("Main: {}", mainNormalizedGrammar);

        final NormGrammarReader normGrammarReader = new NormGrammarReader();

        for(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier : spec.modulesAstSuppliers) {
            final Result<IStrategoTerm, ?> normalizedGrammarTermResult = context.require(toNormalized(astSupplier));
            if(normalizedGrammarTermResult.isErr()) {
                // TODO: nicer error pattern?
                return Result.ofErr(new Exception("Transforming SDF3 grammar of " + astSupplier + " to normal form failed", normalizedGrammarTermResult.getErr()));
            }
            final IStrategoTerm normalizedGrammarTerm = normalizedGrammarTermResult.get();
            log.info("Other: {}", normalizedGrammarTerm);
            normGrammarReader.addModuleAst(normalizedGrammarTerm);
        }

        final NormGrammar normalizedGrammar;
        if(!args.createCompletionTable) {
            try {
                normalizedGrammar = normGrammarReader.readGrammar(mainNormalizedGrammar);
            } catch(Exception e) {
                return Result.ofErr(new Exception("Converting SDF3 normalized grammar ASTs to a NormGrammar failed", e));
            }
        } else {
            // Add main normalized grammar, instead of using it as the main module, since the completion version of the
            // main module is the actual main module in case of creating a completion parse table.
            normGrammarReader.addModuleAst(mainNormalizedGrammar);

            final Result<IStrategoTerm, ?> mainCompletionNormalizedGrammarResult = context.require(toCompletionNormalized(spec.mainModuleAstSupplier));
            if(mainCompletionNormalizedGrammarResult.isErr()) {
                return Result.ofErr(new Exception("Transforming SDF3 grammar of main module " + spec.mainModuleAstSupplier + " to completion normal form failed"));
            }
            final IStrategoTerm mainCompletionNormalizedGrammar = mainCompletionNormalizedGrammarResult.get();
            log.info("Main completion: {}", mainCompletionNormalizedGrammar);

            for(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier : spec.modulesAstSuppliers) {
                final Result<IStrategoTerm, ?> normalizedGrammarTermResult = context.require(toCompletionNormalized(astSupplier));
                if(normalizedGrammarTermResult.isErr()) {
                    return Result.ofErr(new Exception("Transforming SDF3 grammar of " + astSupplier + " to completion normal form failed"));
                }
                final IStrategoTerm normalizedGrammarTerm = normalizedGrammarTermResult.get();
                log.info("Other completion: {}", normalizedGrammarTerm);
                normGrammarReader.addModuleAst(normalizedGrammarTerm);
            }

            try {
                normalizedGrammar = normGrammarReader.readGrammar(mainCompletionNormalizedGrammar);
            } catch(Exception e) {
                return Result.ofErr(new Exception("Converting SDF3 completion normalized grammar ASTs to a completion NormGrammar failed", e));
            }
        }

        return Result.ofOk(new ParseTable(normalizedGrammar, args.parseTableConfiguration));
    }

    private Task<Result<IStrategoTerm, ?>> toNormalized(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier) {
        return toNormalForm.createTask(toPermissive.createSupplier(astSupplier));
    }

    private Task<Result<IStrategoTerm, ?>> toCompletionNormalized(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier) {
        return toNormalForm.createTask(toCompletion.createSupplier(astSupplier));
    }
}
