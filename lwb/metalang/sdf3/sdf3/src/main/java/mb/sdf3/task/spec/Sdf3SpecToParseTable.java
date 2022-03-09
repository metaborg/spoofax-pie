package mb.sdf3.task.spec;

import mb.common.result.ExpectException;
import mb.common.result.Result;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3AstStrategoTransformTaskDef;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToCompletion;
import mb.sdf3.task.Sdf3ToNormalForm;
import mb.sdf3.task.Sdf3ToPermissive;
import mb.sdf3.task.util.Sdf3Util;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.grammar.NormGrammar;
import org.metaborg.sdf2table.io.NormGrammarReader;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Sdf3Scope
public class Sdf3SpecToParseTable implements TaskDef<Sdf3SpecToParseTable.Input, Result<ParseTable, ?>> {
    public static class Input implements Serializable {
        private final Sdf3SpecConfig specConfig;
        private final Sdf3Config config;
        private final String strategyAffix;
        private final boolean createCompletionTable;

        public Input(Sdf3SpecConfig specConfig, Sdf3Config config, String strategyAffix, boolean createCompletionTable) {
            this.specConfig = specConfig;
            this.config = config;
            this.strategyAffix = strategyAffix;
            this.createCompletionTable = createCompletionTable;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input that = (Input)o;
            return this.createCompletionTable == that.createCompletionTable
                && this.specConfig.equals(that.specConfig)
                && this.config.equals(that.config)
                && this.strategyAffix.equals(that.strategyAffix);
        }

        @Override public int hashCode() {
            return Objects.hash(
                this.createCompletionTable,
                this.specConfig,
                this.config,
                this.strategyAffix
            );
        }

        @Override public String toString() {
            return "Sdf3SpecToParseTable$Input{" +
                "specConfig=" + specConfig +
                ", config=" + config +
                ", strategyAffix='" + strategyAffix + '\'' +
                ", createCompletionTable=" + createCompletionTable +
                '}';
        }
    }

    private final Sdf3ClassLoaderResources classLoaderResources;
    private final Sdf3Parse parse;
    private final Sdf3Desugar desugar;
    private final Sdf3ToPermissive toPermissive;
    private final Sdf3ToCompletion toCompletion;
    private final Sdf3ToNormalForm toNormalForm;

    @Inject public Sdf3SpecToParseTable(
        Sdf3ClassLoaderResources classLoaderResources,
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToPermissive toPermissive,
        Sdf3ToCompletion toCompletion,
        Sdf3ToNormalForm toNormalForm
    ) {
        this.classLoaderResources = classLoaderResources;
        this.parse = parse;
        this.desugar = desugar;
        this.toPermissive = toPermissive;
        this.toCompletion = toCompletion;
        this.toNormalForm = toNormalForm;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<ParseTable, ?> exec(ExecContext context, Input input) throws IOException {
        final JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().rootDirectoryHint(input.specConfig.rootDirectory);
        final Supplier<Result<IStrategoTerm, ?>> mainModuleAstSupplier = desugar.createSupplier(parseInputBuilder.withFile(input.specConfig.mainFile).buildAstSupplier());

        final ResourceWalker walker = Sdf3Util.createResourceWalker();
        final ResourceMatcher matcher = Sdf3Util.createResourceMatcher();
        final HierarchicalResource mainSourceDirectory = context.require(input.specConfig.mainSourceDirectory, ResourceStampers.modifiedDirRec(walker, matcher));
        if(!mainSourceDirectory.exists() || !mainSourceDirectory.isDirectory()) {
            return Result.ofErr(new IOException("Main SDF3 source directory '" + mainSourceDirectory +"' does not exist or is not a directory"));
        }
        final ArrayList<Supplier<? extends Result<IStrategoTerm, ?>>> modulesAstSuppliers;
        try(final Stream<? extends HierarchicalResource> stream = mainSourceDirectory.walk(walker, matcher)) {
            modulesAstSuppliers = stream
                .filter(file -> !file.getPath().equals(input.specConfig.mainFile)) // Filter out main module, as it is supplied separately.
                .map(file -> desugar.createSupplier(parseInputBuilder.withFile(file.getKey()).buildAstSupplier()))
                .collect(Collectors.toCollection(ArrayList::new));
        }
        modulesAstSuppliers.add(parseInputBuilder.withFile(classLoaderResources.getDefinitionResource("permissive-water.sdf3").getPath()).buildAstSupplier());

        try {
            final IStrategoTerm mainNormalizedGrammar = context.require(toNormalized(mainModuleAstSupplier, input))
                .expect(e -> new ExpectException("Transforming SDF3 grammar of main module " + mainModuleAstSupplier + " to normal form failed", e));

            final NormGrammarReader normGrammarReader = new NormGrammarReader();
            for(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier : modulesAstSuppliers) {
                final IStrategoTerm normalizedGrammarTerm = context.require(toNormalized(astSupplier, input))
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

                final IStrategoTerm mainCompletionNormalizedGrammar = context.require(toCompletionNormalized(mainModuleAstSupplier, input))
                    .expect(e -> new ExpectException("Transforming SDF3 grammar of main module " + mainModuleAstSupplier + " to completion normal form failed", e));

                for(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier : modulesAstSuppliers) {
                    final IStrategoTerm normalizedGrammarTerm = context.require(toCompletionNormalized(astSupplier, input))
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

            // HACK: remove the "permissive-water" module from the modules read of the normalized grammar, such that
            //       the parenthesizer does not generate an import to its signatures, as it does not have a
            //       corresponding signatures file.
            normalizedGrammar.getModulesRead().remove("normalized/permissive-water-norm");

            return Result.ofOk(new ParseTable(normalizedGrammar, input.specConfig.parseTableConfig));
        } catch(ExpectException e) {
            return Result.ofErr(e);
        }
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    private Task<Result<IStrategoTerm, ?>> toNormalized(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier, Input input) {
        return toNormalForm.createTask(new Sdf3AstStrategoTransformTaskDef.Input(
            toPermissive.createSupplier(astSupplier),
            input.config,
            input.strategyAffix
        ));
    }

    private Task<Result<IStrategoTerm, ?>> toCompletionNormalized(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier, Input input) {
        return toNormalForm.createTask(new Sdf3AstStrategoTransformTaskDef.Input(
            toCompletion.createSupplier(astSupplier),
            input.config,
            input.strategyAffix
        ));
    }
}
