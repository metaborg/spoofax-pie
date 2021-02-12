package mb.spoofax.compiler.spoofax3.standalone;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.Properties;
import mb.log.stream.StreamLoggerFactory;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.task.archive.UnarchiveCommon;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.adapter.data.ArgProviderRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.adapter.data.ParamRepr;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3Compiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProject;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.spoofax3.standalone.dagger.Spoofax3CompilerStandalone;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.platform.BaseResourceServiceComponent;
import mb.spoofax.core.platform.DaggerBaseResourceServiceComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformPieModule;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;

class CompileToJavaClassFilesTest {
    final BaseResourceServiceComponent baseResourceServiceComponent = DaggerBaseResourceServiceComponent.create();
    final ClassLoaderResourceRegistry classLoaderResourceRegistry =
        new ClassLoaderResourceRegistry("spoofax3.standalone", CompileToJavaClassFilesTest.class.getClassLoader());
    final Spoofax3Compiler spoofax3Compiler = new Spoofax3Compiler(
        baseResourceServiceComponent.createChildModule(classLoaderResourceRegistry),
        new LoggerFactoryModule(StreamLoggerFactory.stdOutVeryVerbose()),
        new PlatformPieModule(PieBuilderImpl::new)
    );
    final ResourceService resourceService = spoofax3Compiler.resourceServiceComponent.getResourceService();
    final Spoofax3CompilerStandalone compiler = new Spoofax3CompilerStandalone(spoofax3Compiler);
    final Pie pie = compiler.component.getPie();
    final CompileToJavaClassFiles compileToJavaClassFiles = compiler.component.getCompileToJavaClassFiles();

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
            final @Nullable File file = resourceService.toLocalFile(path);
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
