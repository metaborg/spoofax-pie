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
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spoofax.Sdf3GetSourceFilesWrapper;
import mb.sdf3.task.spoofax.Sdf3ParseWrapper;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;

/**
 * SDF3 generation utilities in the context of the Spoofax LWB compiler.
 */
public class SpoofaxSdf3GenerationUtil {
    private final SpoofaxSdf3Configure configure;
    private final Sdf3ParseWrapper parse;
    private final Sdf3GetSourceFilesWrapper getSourceFiles;
    private final Sdf3Desugar desugar;
    private final Sdf3AnalyzeMulti analyze;

    @Inject public SpoofaxSdf3GenerationUtil(
        SpoofaxSdf3Configure configure,
        Sdf3ParseWrapper parse,
        Sdf3GetSourceFilesWrapper getSourceFiles,
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
    ) throws E, InterruptedException, IOException, SpoofaxSdf3ConfigureException {
        final Result<Option<SpoofaxSdf3Config>, SpoofaxSdf3ConfigureException> configureResult = context.require(configure, rootDirectory);
        final Option<SpoofaxSdf3Config> configureOption = configureResult.unwrap();
        if(configureOption.isSome()) {
            final SpoofaxSdf3Config spoofaxSdf3Config = configureOption.unwrap();
            if(!spoofaxSdf3Config.getMainSdf3SpecConfig().isSome()) {
                return; // Only generate when there are SDF3 source files (not prebuilt).
            }
            final Sdf3SpecConfig config = spoofaxSdf3Config.getMainSdf3SpecConfig().unwrap();
            final JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().rootDirectoryHint(rootDirectory);
            final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(config.rootDirectory, parse.createRecoverableMultiAstSupplierFunction(getSourceFiles.createFunction()));
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
