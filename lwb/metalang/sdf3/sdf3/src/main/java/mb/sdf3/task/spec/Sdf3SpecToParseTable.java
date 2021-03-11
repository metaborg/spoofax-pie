package mb.sdf3.task.spec;

import mb.common.result.ExpectException;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3ToCompletion;
import mb.sdf3.task.Sdf3ToNormalForm;
import mb.sdf3.task.Sdf3ToPermissive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.io.NormGrammarReader;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

@Sdf3Scope
public class Sdf3SpecToParseTable implements TaskDef<Sdf3SpecToParseTable.Input, Result<ParseTable, ?>> {
    public static class Input implements Serializable {
        private final Supplier<Result<Sdf3Spec, ?>> specSupplier;
        private final boolean createCompletionTable;

        public Input(Supplier<Result<Sdf3Spec, ?>> specSupplier, boolean createCompletionTable) {
            this.specSupplier = specSupplier;
            this.createCompletionTable = createCompletionTable;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(createCompletionTable != input.createCompletionTable) return false;
            return specSupplier.equals(input.specSupplier);
        }

        @Override public int hashCode() {
            int result = specSupplier.hashCode();
            result = 31 * result + (createCompletionTable ? 1 : 0);
            return result;
        }

        @Override public String toString() {
            return "Input{" +
                "specSupplier=" + specSupplier +
                ", createCompletionTable=" + createCompletionTable +
                '}';
        }
    }

    private final Sdf3ToPermissive toPermissive;
    private final Sdf3ToCompletion toCompletion;
    private final Sdf3ToNormalForm toNormalForm;

    @Inject public Sdf3SpecToParseTable(
        Sdf3ToPermissive toPermissive,
        Sdf3ToCompletion toCompletion,
        Sdf3ToNormalForm toNormalForm
    ) {
        this.toPermissive = toPermissive;
        this.toCompletion = toCompletion;
        this.toNormalForm = toNormalForm;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<ParseTable, ?> exec(ExecContext context, Input input) throws IOException {
        final Result<Sdf3Spec, ?> specResult = context.require(input.specSupplier);
        if(specResult.isErr()) {
            return Result.ofErr(specResult.getErr());
        }
        final Sdf3Spec spec = specResult.get();

        try {
            final IStrategoTerm mainNormalizedGrammar = context.require(toNormalized(spec.mainModuleAstSupplier))
                .expect(e -> new ExpectException("Transforming SDF3 grammar of main module " + spec.mainModuleAstSupplier + " to normal form failed", e));

            final NormGrammarReader normGrammarReader = new NormGrammarReader();
            for(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier : spec.modulesAstSuppliers) {
                final IStrategoTerm normalizedGrammarTerm = context.require(toNormalized(astSupplier))
                    .expect(e -> new ExpectException("Transforming SDF3 grammar of " + astSupplier + " to normal form failed", e));
                normGrammarReader.addModuleAst(normalizedGrammarTerm);
            }

            final NormGrammar normalizedGrammar;
            if(!input.createCompletionTable) {
                try {
                    normalizedGrammar = normGrammarReader.readGrammar(mainNormalizedGrammar);
                } catch(RuntimeException e) {
                    throw e; // Do not wrap runtime exceptions, rethrow them.
                } catch(Exception e) {
                    return Result.ofErr(new Exception("Converting SDF3 normalized grammar ASTs to a NormGrammar failed", e));
                }
            } else {
                // Add main normalized grammar, instead of using it as the main module, since the completion version of the
                // main module is the actual main module in case of creating a completion parse table.
                normGrammarReader.addModuleAst(mainNormalizedGrammar);

                final IStrategoTerm mainCompletionNormalizedGrammar = context.require(toCompletionNormalized(spec.mainModuleAstSupplier))
                    .expect(e -> new ExpectException("Transforming SDF3 grammar of main module " + spec.mainModuleAstSupplier + " to completion normal form failed", e));

                for(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier : spec.modulesAstSuppliers) {
                    final IStrategoTerm normalizedGrammarTerm = context.require(toCompletionNormalized(astSupplier))
                        .expect(e -> new ExpectException("Transforming SDF3 grammar of " + astSupplier + " to completion normal form failed", e));
                    normGrammarReader.addModuleAst(normalizedGrammarTerm);
                }

                try {
                    normalizedGrammar = normGrammarReader.readGrammar(mainCompletionNormalizedGrammar);
                } catch(RuntimeException e) {
                    throw e; // Do not wrap runtime exceptions, rethrow them.
                } catch(Exception e) {
                    return Result.ofErr(new Exception("Converting SDF3 completion normalized grammar ASTs to a completion NormGrammar failed", e));
                }
            }

            return Result.ofOk(new ParseTable(normalizedGrammar, spec.parseTableConfig));
        } catch(ExpectException e) {
            return Result.ofErr(e);
        }
    }

    private Task<Result<IStrategoTerm, ?>> toNormalized(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier) {
        return toNormalForm.createTask(toPermissive.createSupplier(astSupplier));
    }

    private Task<Result<IStrategoTerm, ?>> toCompletionNormalized(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier) {
        return toNormalForm.createTask(toCompletion.createSupplier(astSupplier));
    }
}
