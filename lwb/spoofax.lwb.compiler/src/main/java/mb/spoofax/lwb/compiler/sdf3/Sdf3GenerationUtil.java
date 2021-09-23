package mb.spoofax.lwb.compiler.sdf3;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3GetSourceFiles;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;

public class Sdf3GenerationUtil {
    private final ConfigureSdf3 configure;
    private final Sdf3Parse parse;
    private final Sdf3GetSourceFiles getSourceFiles;
    private final Sdf3Desugar desugar;
    private final Sdf3AnalyzeMulti analyze;

    @Inject public Sdf3GenerationUtil(
        ConfigureSdf3 configure,
        Sdf3Parse parse,
        Sdf3GetSourceFiles getSourceFiles,
        Sdf3Desugar desugar,
        Sdf3AnalyzeMulti analyze
    ) {
        this.configure = configure;
        this.parse = parse;
        this.getSourceFiles = getSourceFiles;
        this.desugar = desugar;
        this.analyze = analyze;
    }


    public interface Callbacks<E extends Exception> {
        default void generateFromAst(ExecContext context, STask<Result<IStrategoTerm, ?>> astSupplier) throws E, IOException, InterruptedException {}

        default void generateFromAnalyzed(ExecContext context, Supplier<Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> singleFileAnalysisOutputSupplier) throws E, IOException, InterruptedException {}

        default void generateFromConfig(ExecContext context, Sdf3SpecConfig sdf3Config) throws E, IOException, InterruptedException {}
    }

    public <E extends Exception> void performSdf3GenerationIfEnabled(
        ExecContext context,
        ResourcePath rootDirectory,
        Callbacks<E> callbacks
    ) throws E, InterruptedException, IOException, Sdf3ConfigureException {
        final Result<Option<Sdf3SpecConfig>, Sdf3ConfigureException> configureResult = context.require(configure, rootDirectory);
        configureResult.throwIfError();
        // noinspection ConstantConditions (value is present)
        final Option<Sdf3SpecConfig> configureOption = configureResult.get();
        // noinspection ConstantConditions (value is present)
        if(configureOption.isSome()) {
            // noinspection ConstantConditions (value is present)
            final Sdf3SpecConfig config = configureOption.get();
            final JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().rootDirectoryHint(rootDirectory);
            final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(config.mainSourceDirectory, parse.createRecoverableMultiAstSupplierFunction(getSourceFiles.createFunction()));
            for(ResourcePath file : context.require(getSourceFiles, rootDirectory)) {
                final Supplier<Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> singleFileAnalysisOutputSupplier = analyze.createSingleFileOutputSupplier(analyzeInput, file);
                callbacks.generateFromAnalyzed(context, singleFileAnalysisOutputSupplier);
                final STask<Result<IStrategoTerm, ?>> astSupplier = desugar.createSupplier(parseInputBuilder.withFile(file).buildAstSupplier());
                callbacks.generateFromAst(context, astSupplier);
            }
            callbacks.generateFromConfig(context, config);
        }
    }
}
