package mb.spoofax.lwb.compiler;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.dagger.PieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.task.archive.UnarchiveCommon;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.dagger.DaggerRootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceModule;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.adapter.data.ArgProviderRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.adapter.data.ParamRepr;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.lwb.compiler.dagger.Spoofax3Compiler;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageInput;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageInputBuilder;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageShared;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageToJavaClassPathInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class CompileLanguageToJavaClassPathTest {
    final LoggerComponent loggerComponent = DaggerLoggerComponent.builder()
        .loggerModule(LoggerModule.stdOutVeryVerbose())
        .build();
    final ClassLoaderResourceRegistry classLoaderResourceRegistry =
        new ClassLoaderResourceRegistry("language-compiler", CompileLanguageToJavaClassPathTest.class.getClassLoader());
    final RootResourceServiceComponent rootResourceServiceComponent = DaggerRootResourceServiceComponent.builder()
        .rootResourceServiceModule(new RootResourceServiceModule(classLoaderResourceRegistry))
        .loggerComponent(loggerComponent)
        .build();
    final Spoofax3Compiler compiler = new Spoofax3Compiler(
        loggerComponent,
        rootResourceServiceComponent.createChildModule(classLoaderResourceRegistry),
        new PieModule(PieBuilderImpl::new)
    );
    final ResourceService resourceService = compiler.resourceServiceComponent.getResourceService();
    final Pie pie = compiler.pieComponent.getPie();
    final CompileLanguageToJavaClassPath compileLanguageToJavaClassPath = compiler.component.getCompileLanguageToJavaClassPath();

    @Test void testCompileCharsLanguage(@TempDir Path temporaryDirectoryPath) throws Exception {
        // Copy language specification sources to the temporary directory.
        final FSResource temporaryDirectory = new FSResource(temporaryDirectoryPath);
        copyResourcesToTemporaryDirectory("mb/spoofax/lwb/compiler/chars", temporaryDirectory);

        // Create the compiler inputs.
        final String packageId = "mb.chars";
        final Shared shared = Shared.builder()
            .name("Chars")
            .defaultPackageId(packageId)
            .defaultClassPrefix("Chars")
            .build();

        final LanguageProject languageProject = LanguageProject.builder().withDefaults(temporaryDirectory.getPath(), shared).build();
        final CompileLanguageShared languageSpecification = CompileLanguageShared.builder().languageProject(languageProject).build();
        final AdapterProject adapterProject = AdapterProject.builder().withDefaults(temporaryDirectory.getPath(), shared).build();

        final LanguageProjectCompilerInputBuilder languageProjectInputBuilder = new LanguageProjectCompilerInputBuilder();
        final CompileLanguageInputBuilder compileLanguageInputBuilder = new CompileLanguageInputBuilder();
        final AdapterProjectCompilerInputBuilder adapterProjectCompilerInputBuilder = new AdapterProjectCompilerInputBuilder();

        compileLanguageInputBuilder.withSdf3();
        compileLanguageInputBuilder.withEsv();
        final CompileLanguageInput compileLanguageInput = compileLanguageInputBuilder.build(new Properties(), shared, languageSpecification);
        compileLanguageInput.syncTo(languageProjectInputBuilder);

        languageProjectInputBuilder.withParser().startSymbol("Start");
        languageProjectInputBuilder.withStyler();
        final LanguageProjectCompiler.Input languageProjectInput = languageProjectInputBuilder.build(shared, languageProject);

        adapterProjectCompilerInputBuilder.withParser();
        adapterProjectCompilerInputBuilder.withStyler();
        final TypeInfo showAst = TypeInfo.of(packageId, "CharsShowAst");
        adapterProjectCompilerInputBuilder.project.addTaskDefs(showAst);
        final CommandDefRepr showAstCommand = CommandDefRepr.builder()
            .type(packageId, "CalcShowToJavaCommand")
            .taskDefType(showAst)
            .argType(showAst.appendToId(".Args"))
            .displayName("Show parsed AST")
            .description("Shows the parsed AST")
            .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
            .addParams(
                ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File))
            )
            .build();
        adapterProjectCompilerInputBuilder.project.addCommandDefs(showAstCommand);
        final AdapterProjectCompiler.Input adapterProjectInput = adapterProjectCompilerInputBuilder.build(languageProjectInput, Option.ofNone(), adapterProject);

        // Combine inputs and run the compiler.
        final CompileLanguageToJavaClassPathInput input = CompileLanguageToJavaClassPathInput.builder()
            .shared(shared)
            .languageProjectInput(languageProjectInput)
            .compileLanguageInput(compileLanguageInput)
            .adapterProjectInput(adapterProjectInput)
            .build();
        final Result<CompileLanguageToJavaClassPath.Output, CompileLanguageToJavaClassPathException> result;
        try(final MixedSession session = pie.newSession()) {
            result = session.require(compileLanguageToJavaClassPath.createTask(input));
        }
        try {
            result.unwrap();
        } catch(CompileLanguageToJavaClassPathException e) {
            System.err.println(e.getMessage());
            // TODO: print sub messages
//            e.getSubMessage().ifPresent(System.err::println);
//            e.getSubMessages().ifPresent(System.err::println);
            throw e;
        }
    }


    void copyResourcesToTemporaryDirectory(String sourceFilesPath, HierarchicalResource temporaryDirectory) throws IOException {
        final ClassLoaderResource sourceFilesDirectory = classLoaderResourceRegistry.getResource(sourceFilesPath);
        final ClassLoaderResourceLocations locations = sourceFilesDirectory.getLocations();
        for(FSResource directory : locations.directories) {
            directory.copyRecursivelyTo(temporaryDirectory);
        }
        for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
            UnarchiveCommon.unarchiveJar(jarFileWithPath.file, temporaryDirectory, false, false);
        }
    }
}
