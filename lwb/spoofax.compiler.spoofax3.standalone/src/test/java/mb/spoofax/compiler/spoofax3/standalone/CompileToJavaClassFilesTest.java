package mb.spoofax.compiler.spoofax3.standalone;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.esv.DaggerEsvComponent;
import mb.libspoofax2.DaggerLibSpoofax2Component;
import mb.libstatix.DaggerLibStatixComponent;
import mb.log.stream.StreamLoggerFactory;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.task.archive.UnarchiveCommon;
import mb.pie.task.java.CompileJava;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.DaggerSdf3Component;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.adapter.data.ArgProviderRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.adapter.data.ParamRepr;
import mb.spoofax.compiler.dagger.DaggerSpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.spoofax3.dagger.DaggerSpoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerModule;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProject;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceRegistriesModule;
import mb.statix.DaggerStatixComponent;
import mb.str.DaggerStrategoComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;

class CompileToJavaClassFilesTest {
    final ClassLoaderResourceRegistry classLoaderResourceRegistry = new ClassLoaderResourceRegistry("spoofax3.standalone", CompileToJavaClassFilesTest.class.getClassLoader());
    final PlatformComponent platformComponent = DaggerPlatformComponent.builder()
        .loggerFactoryModule(new LoggerFactoryModule(StreamLoggerFactory.stdOutVeryVerbose()))
        .resourceRegistriesModule(new ResourceRegistriesModule(classLoaderResourceRegistry))
        .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
        .build();
    final TemplateCompiler templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
    final SpoofaxCompilerComponent spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
        .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler, PieBuilderImpl::new))
        .build();
    final Spoofax3CompilerComponent spoofax3CompilerComponent = DaggerSpoofax3CompilerComponent.builder()
        .spoofax3CompilerModule(new Spoofax3CompilerModule(templateCompiler))
        .platformComponent(platformComponent)
        .sdf3Component(DaggerSdf3Component.builder().platformComponent(platformComponent).build())
        .strategoComponent(DaggerStrategoComponent.builder().platformComponent(platformComponent).build())
        .esvComponent(DaggerEsvComponent.builder().platformComponent(platformComponent).build())
        .statixComponent(DaggerStatixComponent.builder().platformComponent(platformComponent).build())
        .libSpoofax2Component(DaggerLibSpoofax2Component.builder().platformComponent(platformComponent).build())
        .libStatixComponent(DaggerLibStatixComponent.builder().platformComponent(platformComponent).build())
        .build();

    // HACK: manually create task definitions and create a child PIE instance, instead of using dagger.
    final CompileJava compileJava = new CompileJava();
    final CompileToJavaClassFiles compileToJavaClassFiles = new CompileToJavaClassFiles(
        spoofax3CompilerComponent.getResourceService(),
        spoofaxCompilerComponent.getLanguageProjectCompiler(),
        spoofax3CompilerComponent.getSpoofax3LanguageProjectCompiler(),
        spoofaxCompilerComponent.getAdapterProjectCompiler(),
        compileJava
    );
    final Pie pie = spoofax3CompilerComponent.getPie().createChildBuilder(spoofaxCompilerComponent.getPie())
        .addTaskDefs(new MapTaskDefs(compileJava, compileToJavaClassFiles))
        .build();


    @Test void testCompileCharsLanguage(@TempDir Path temporaryDirectoryPath) throws Exception {
        // Copy language specification sources to the temporary directory.
        final FSResource temporaryDirectory = new FSResource(temporaryDirectoryPath);
        copyResourcesToTemporaryDirectory("mb/spoofax/compiler/spoofax3/standalone/chars", temporaryDirectory);

        // Create the compiler inputs.
        final String packageId = "mb.chars";
        final Shared shared = Shared.builder()
            .name("Chars")
            .defaultPackageId(packageId)
            .defaultClassPrefix("Chars")
            .build();

        final LanguageProject languageProject = LanguageProject.builder().withDefaults(temporaryDirectory.getPath(), shared).build();
        final Spoofax3LanguageProject spoofax3LanguageProject = Spoofax3LanguageProject.builder().languageProject(languageProject).build();
        final AdapterProject adapterProject = AdapterProject.builder().withDefaults(temporaryDirectory.getPath(), shared).build();

        final LanguageProjectCompilerInputBuilder languageProjectInputBuilder = new LanguageProjectCompilerInputBuilder();
        final Spoofax3LanguageProjectCompilerInputBuilder spoofax3LanguageProjectInputBuilder = new Spoofax3LanguageProjectCompilerInputBuilder();
        final AdapterProjectCompilerInputBuilder adapterProjectCompilerInputBuilder = new AdapterProjectCompilerInputBuilder();

        spoofax3LanguageProjectInputBuilder.withParser();
        spoofax3LanguageProjectInputBuilder.withStyler();
        final Spoofax3LanguageProjectCompiler.Input spoofax3LanguageProjectInput = spoofax3LanguageProjectInputBuilder.build(new Properties(), shared, spoofax3LanguageProject);
        spoofax3LanguageProjectInput.syncTo(languageProjectInputBuilder);

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
        final CompileToJavaClassFiles.Input input = CompileToJavaClassFiles.Input.builder()
            .languageProjectInput(languageProjectInput)
            .spoofax3LanguageProjectInput(spoofax3LanguageProjectInput)
            .adapterProjectInput(adapterProjectInput)
            .build();
        final Result<CompileToJavaClassFiles.Output, CompileToJavaClassFiles.CompileException> result;
        try(final MixedSession session = pie.newSession()) {
            result = session.require(compileToJavaClassFiles.createTask(input));
        }
        final CompileToJavaClassFiles.Output output = result.unwrap();

        // Dynamically load the Main class and use it to parse and show the AST of a file.
        final ArrayList<URL> classPath = new ArrayList<>();
        for(ResourcePath path : output.classPath()) {
            final @Nullable File file = spoofax3CompilerComponent.getResourceService().toLocalFile(path);
            if(file == null) {
                throw new Exception("Cannot dynamically load compiled Java classes or resources from '" + path + "', it is not a directory on the local file system");
            }
            classPath.add(file.toURI().toURL());
        }
        try(final URLClassLoader classLoader = new URLClassLoader(classPath.toArray(new URL[0]), getClass().getClassLoader())) {
            final Class<?> mainClass = classLoader.loadClass(packageId + ".Main");
            final FSResource fileToParse = temporaryDirectory.appendSegment("test.chars");
            fileToParse.ensureFileExists().writeString("abcdefg");
            mainClass.getDeclaredMethod("main", String[].class).invoke(null, new Object[]{new String[]{fileToParse.getJavaPath().toString()}});
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
