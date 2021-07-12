package mb.spoofax.lwb.compiler.sdf3;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.StreamIterable;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.util.Sdf3Util;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.stream.Stream;

public class Sdf3GenerationUtil {
    private final ConfigureSdf3 configureSdf3;
    private final Sdf3Parse sdf3Parse;
    private final Sdf3Desugar sdf3Desugar;
    private final Sdf3AnalyzeMulti sdf3Analyze;

    @Inject public Sdf3GenerationUtil(
        ConfigureSdf3 configureSdf3,
        Sdf3Parse sdf3Parse,
        Sdf3Desugar sdf3Desugar,
        Sdf3AnalyzeMulti sdf3Analyze
    ) {
        this.configureSdf3 = configureSdf3;
        this.sdf3Parse = sdf3Parse;
        this.sdf3Desugar = sdf3Desugar;
        this.sdf3Analyze = sdf3Analyze;
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
        // Compile each SDF3 source file (if SDF3 is enabled) to a Stratego signature, pretty-printer, completion
        // runtime, and injection explication (if enabled) module.
        final Result<Option<Sdf3SpecConfig>, Sdf3ConfigureException> configureResult = context.require(configureSdf3, rootDirectory);
        configureResult.throwIfError();
        // noinspection ConstantConditions (value is present)
        final Option<Sdf3SpecConfig> configureOption = configureResult.get();
        // noinspection ConstantConditions (value is present)
        if(configureOption.isSome()) {
            // noinspection ConstantConditions (value is present)
            final Sdf3SpecConfig config = configureOption.get();
            final ResourceWalker walker = Sdf3Util.createResourceWalker();
            final ResourceMatcher matcher = Sdf3Util.createResourceMatcher();
            final JsglrParseTaskInput.Builder parseInputBuilder = sdf3Parse.inputBuilder().rootDirectoryHint(rootDirectory);
            final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(config.mainSourceDirectory, sdf3Parse.createRecoverableMultiAstSupplierFunction(walker, matcher));
            final HierarchicalResource sdfMainSourceDirectory = context.require(config.mainSourceDirectory, ResourceStampers.modifiedDirRec(walker, matcher));
            try(final Stream<? extends HierarchicalResource> stream = sdfMainSourceDirectory.walk(walker, matcher)) {
                for(HierarchicalResource file : new StreamIterable<>(stream)) {
                    final Supplier<Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> singleFileAnalysisOutputSupplier = sdf3Analyze.createSingleFileOutputSupplier(analyzeInput, file.getPath());
                    callbacks.generateFromAnalyzed(context, singleFileAnalysisOutputSupplier);
                    final STask<Result<IStrategoTerm, ?>> astSupplier = sdf3Desugar.createSupplier(parseInputBuilder.withFile(file.getPath()).buildAstSupplier());
                    callbacks.generateFromAst(context, astSupplier);
                }
            }
            callbacks.generateFromConfig(context, config);
        }
    }
}
