package mb.spoofax.lwb.compiler.statix;

import mb.cfg.CompileLanguageInput;
import mb.cfg.CompileLanguageSpecificationInput;
import mb.cfg.CompileLanguageSpecificationShared;
import mb.cfg.metalang.CompileStatixInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3_ext_statix.task.Sdf3ExtStatixGenerateStatix;
import mb.spoofax.lwb.compiler.sdf3.Sdf3ConfigureException;
import mb.spoofax.lwb.compiler.sdf3.Sdf3GenerationUtil;
import mb.statix.task.StatixConfig;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class ConfigureStatix implements TaskDef<ResourcePath, Result<Option<StatixConfig>, StatixConfigureException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final StatixGenerationUtil statixGenerationUtil;

    private final Sdf3GenerationUtil sdf3GenerationUtil;
    private final Sdf3ExtStatixGenerateStatix sdf3ExtStatixGenerateStatix;

    @Inject public ConfigureStatix(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        StatixGenerationUtil statixGenerationUtil,
        Sdf3GenerationUtil sdf3GenerationUtil,
        Sdf3ExtStatixGenerateStatix sdf3ExtStatixGenerateStatix
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.statixGenerationUtil = statixGenerationUtil;
        this.sdf3GenerationUtil = sdf3GenerationUtil;
        this.sdf3ExtStatixGenerateStatix = sdf3ExtStatixGenerateStatix;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<StatixConfig>, StatixConfigureException> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(StatixConfigureException::getLanguageCompilerConfigurationFail)
            .<Option<StatixConfig>, Exception>flatMapThrowing(cfgOutput -> Result.transpose(Option.ofOptional(cfgOutput.compileLanguageInput.compileLanguageSpecificationInput().statix())
                .mapThrowing(statixInput -> toStatixConfig(context, rootDirectory, cfgOutput.compileLanguageInput, statixInput))
            ));
    }


    public Result<StatixConfig, StatixConfigureException> toStatixConfig(
        ExecContext context,
        ResourcePath rootDirectory,
        CompileLanguageInput input,
        CompileStatixInput statixInput
    ) throws IOException, InterruptedException {
        final HierarchicalResource mainSourceDirectory = context.require(statixInput.mainSourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainSourceDirectory.exists() || !mainSourceDirectory.isDirectory()) {
            return Result.ofErr(StatixConfigureException.mainSourceDirectoryFail(mainSourceDirectory.getPath()));
        }
        final HierarchicalResource mainFile = context.require(statixInput.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(StatixConfigureException.mainFileFail(mainFile.getPath()));
        }
        // TODO: check include directories.
        // TODO: check source directories (if added to input).

        // Gather origins for provided Statix files.
        final ArrayList<STask<?>> sourceFileOrigins = new ArrayList<>();
        // Gather include directories. Use LinkedHashSet to remove duplicates while keeping insertion order.
        final LinkedHashSet<ResourcePath> includeDirectories = new LinkedHashSet<>(statixInput.includeDirectories());

        // Compile each SDF3 source file (if SDF3 is enabled) to a Statix signature module (if enabled).
        final CompileLanguageSpecificationInput compileLanguageSpecificationInput = input.compileLanguageSpecificationInput();
        final CompileLanguageSpecificationShared compileLanguageSpecificationShared = compileLanguageSpecificationInput.compileLanguageShared();
        final ResourcePath generatedSourcesDirectory = compileLanguageSpecificationShared.generatedStatixSourcesDirectory();
        if(statixInput.enableSdf3SignatureGen()) {
            try {
                sdf3GenerationUtil.performSdf3GenerationIfEnabled(context, rootDirectory, new Sdf3GenerationUtil.Callbacks<StatixConfigureException>() {
                    @Override
                    public void generateFromAst(ExecContext context, STask<Result<IStrategoTerm, ?>> astSupplier) throws StatixConfigureException, InterruptedException {
                        try {
                            sdf3ToStatixGenInj(context, generatedSourcesDirectory, astSupplier);
                        } catch(RuntimeException | InterruptedException e) {
                            throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
                        } catch(Exception e) {
                            throw StatixConfigureException.sdf3ExtStatixGenInjFail(e);
                        }
                    }

                    @Override
                    public void generateFromConfig(ExecContext context, Sdf3SpecConfig sdf3Config) {
                        // Add generated sources directory as an include Statix imports.
                        includeDirectories.add(generatedSourcesDirectory);
                        // Add this as an origin, as this task provides the Statix files (in statixGenerationUtil.writePrettyPrintedFile).
                        sourceFileOrigins.add(createSupplier(rootDirectory));
                    }
                });
            } catch(StatixConfigureException e) {
                return Result.ofErr(e);
            } catch(Sdf3ConfigureException e) {
                return Result.ofErr(StatixConfigureException.sdf3ConfigureFail(e));
            }
        }

        return Result.ofOk(new StatixConfig(
            rootDirectory,
            mainFile.getPath(),
            ListView.of(mainSourceDirectory.getPath()),
            ListView.copyOf(includeDirectories),
            ListView.of(sourceFileOrigins)
        ));
    }

    private void sdf3ToStatixGenInj(
        ExecContext context,
        ResourcePath generatesSourcesDirectory,
        STask<Result<IStrategoTerm, ?>> astSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ExtStatixGenerateStatix.createSupplier(astSupplier);
        statixGenerationUtil.writePrettyPrintedFile(context, generatesSourcesDirectory, supplier);
    }
}
