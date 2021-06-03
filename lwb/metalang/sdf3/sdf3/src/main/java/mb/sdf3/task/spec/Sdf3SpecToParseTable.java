package mb.sdf3.task.spec;

import mb.common.result.ExpectException;
import mb.common.result.Result;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.Sdf3Scope;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Sdf3Scope
public class Sdf3SpecToParseTable implements TaskDef<Sdf3SpecToParseTable.Input, Result<ParseTable, ?>> {
    public static class Input implements Serializable {
        private final Sdf3SpecConfig config;
        private final boolean createCompletionTable;

        public Input(Sdf3SpecConfig config, boolean createCompletionTable) {
            this.config = config;
            this.createCompletionTable = createCompletionTable;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(createCompletionTable != input.createCompletionTable) return false;
            return config.equals(input.config);
        }

        @Override public int hashCode() {
            int result = config.hashCode();
            result = 31 * result + (createCompletionTable ? 1 : 0);
            return result;
        }

        @Override public String toString() {
            return "Sdf3SpecToParseTable$Input{" +
                "config=" + config +
                ", createCompletionTable=" + createCompletionTable +
                '}';
        }
    }

    private final Sdf3Parse parse;
    private final Sdf3Desugar desugar;
    private final Sdf3ToPermissive toPermissive;
    private final Sdf3ToCompletion toCompletion;
    private final Sdf3ToNormalForm toNormalForm;

    @Inject public Sdf3SpecToParseTable(
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3ToPermissive toPermissive,
        Sdf3ToCompletion toCompletion,
        Sdf3ToNormalForm toNormalForm
    ) {
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
        final JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().rootDirectoryHint(input.config.rootDirectory);
        final Supplier<Result<IStrategoTerm, ?>> mainModuleAstSupplier = desugar.createSupplier(parseInputBuilder.withFile(input.config.mainFile).buildAstSupplier());

        final ResourceWalker walker = Sdf3Util.createResourceWalker();
        final ResourceMatcher matcher = Sdf3Util.createResourceMatcher();
        final HierarchicalResource mainSourceDirectory = context.require(input.config.mainSourceDirectory, ResourceStampers.modifiedDirRec(walker, matcher));
        context.require(mainSourceDirectory, ResourceStampers.modifiedDirRec(walker, matcher));
        final ArrayList<Supplier<Result<IStrategoTerm, ?>>> modulesAstSuppliers;
        try(final Stream<? extends HierarchicalResource> stream = mainSourceDirectory.walk(walker, matcher)) {
            modulesAstSuppliers = stream
                .filter(file -> !file.getPath().equals(input.config.mainFile)) // Filter out main module, as it is supplied separately.
                .map(file -> desugar.createSupplier(parseInputBuilder.withFile(file.getKey()).buildAstSupplier()))
                .collect(Collectors.toCollection(ArrayList::new));
        }

        try {
            final IStrategoTerm mainNormalizedGrammar = context.require(toNormalized(mainModuleAstSupplier))
                .expect(e -> new ExpectException("Transforming SDF3 grammar of main module " + mainModuleAstSupplier + " to normal form failed", e));

            final NormGrammarReader normGrammarReader = new NormGrammarReader();
            for(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier : modulesAstSuppliers) {
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

                final IStrategoTerm mainCompletionNormalizedGrammar = context.require(toCompletionNormalized(mainModuleAstSupplier))
                    .expect(e -> new ExpectException("Transforming SDF3 grammar of main module " + mainModuleAstSupplier + " to completion normal form failed", e));

                for(Supplier<? extends Result<IStrategoTerm, ?>> astSupplier : modulesAstSuppliers) {
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

            return Result.ofOk(new ParseTable(normalizedGrammar, input.config.parseTableConfig));
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
