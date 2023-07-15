package mb.str;

import mb.common.message.KeyedMessages;
import mb.common.result.MessagesException;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.pie.api.MixedSession;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.ValueSupplier;
import mb.pie.task.archive.ArchiveDirectory;
import mb.pie.task.archive.ArchiveToJar;
import mb.pie.task.java.CompileJava;
import mb.resource.fs.FSResource;
import mb.str.config.StrategoCompileConfig;
import mb.str.config.StrategoConfig;
import mb.str.task.StrategoCompileToJava;
import mb.str.util.TestBase;
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.strategolib.StrategoLibInfo;
import mb.strategolib.StrategoLibUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CompileTest extends TestBase {
    @Test void testCompileAndRun() throws Exception {
        final FSResource strategoSourceDir = directory(rootDirectory, "str");
        final FSResource strategoMainFile = textFile(strategoSourceDir, "hello.str", "" +
            "module hello " +
            "imports " +
            "  strategolib " +
            "  world " +
            "rules " +
            "  hello = !$[Hello, [<world>]]"
        );
        final FSResource strategoWorldFile = textFile(strategoSourceDir, "world.str", "" +
            "module world " +
            "imports " +
            "  strategolib " +
            "rules " +
            "  world = !\"world!\""
        );

        final FSResource buildDir = directory(rootDirectory, "build");

        final FSResource strategoJavaSourceFileOutputDir = directory(buildDir, "java");
        final FSResource strategoJavaClassFileOutputDir = directory(buildDir, "classes");
        final FSResource strategoLibUnarchiveDirectory = directory(buildDir, "strategoLib");

        final LinkedHashSet<Supplier<Stratego2LibInfo>> str2Libs = new LinkedHashSet<>();
        final StrategoLibUtil strategoLibUtil = strategoLibComponent.getStrategoLibUtil();
        final Supplier<StrategoLibInfo> strategoLibInfoSupplier = strategoLibUtil.getStrategoLibInfo(strategoLibUnarchiveDirectory.getPath(), unarchiveFromJar);
        str2Libs.add(strategoLibInfoSupplier.map(new ToStratego2LibInfo()));
        final LinkedHashSet<File> javaClassPaths = new LinkedHashSet<>(strategoLibUtil.getStrategoLibJavaClassPaths());

        try(final MixedSession session = newSession()) {
            // Compile Stratego source files to Java source files.
            final StrategoCompileConfig config = new StrategoCompileConfig(
                strategoSourceDir.getPath(),
                StrategoConfig.fromRootDirectoryAndMainFile(strategoSourceDir.getPath(), strategoMainFile.getPath()),
                ListView.of(strategoSourceDir.getPath()),
                ListView.of(),
                ListView.copyOf(str2Libs),
                new Arguments(),
                MapView.of(),
                MapView.of(),
                ListView.of(),
                null,
                strategoJavaSourceFileOutputDir.getPath(),
                strategoJavaClassFileOutputDir.getPath(),
                "mb.test",
                "test",
                ListView.copyOf(javaClassPaths),
                true,
                true
            );
            final Task<Result<StrategoCompileToJava.Output, MessagesException>> strategoCompileTask = compile.createTask(config);
            final Result<StrategoCompileToJava.Output, ?> result = session.require(strategoCompileTask);
            assertOk(result);

            final FSResource strategoJavaSourceFilePackageOutputDir = strategoJavaSourceFileOutputDir.appendRelativePath("mb/test");
            assertTrue(strategoJavaSourceFilePackageOutputDir.exists());
            assertTrue(strategoJavaSourceFilePackageOutputDir.isDirectory());
            final FSResource interopRegistererJavaFile = strategoJavaSourceFilePackageOutputDir.appendRelativePath("InteropRegisterer.java");
            assertTrue(interopRegistererJavaFile.exists());
            assertTrue(interopRegistererJavaFile.isFile());
            assertTrue(interopRegistererJavaFile.readString().contains("InteropRegisterer"));
            final FSResource mainJavaFile = strategoJavaSourceFilePackageOutputDir.appendRelativePath("Main.java");
            assertTrue(mainJavaFile.exists());
            assertTrue(mainJavaFile.isFile());
            assertTrue(mainJavaFile.readString().contains("Main"));
            final FSResource mainStrategyJavaFile = strategoJavaSourceFilePackageOutputDir.appendRelativePath("hello_0_0.java");
            assertTrue(mainStrategyJavaFile.exists());
            assertTrue(mainStrategyJavaFile.isFile());
            assertTrue(mainStrategyJavaFile.readString().contains("hello_0_0"));
            final FSResource worldStrategyJavaFile = strategoJavaSourceFilePackageOutputDir.appendRelativePath("world_0_0.java");
            assertTrue(worldStrategyJavaFile.exists());
            assertTrue(worldStrategyJavaFile.isFile());
            assertTrue(worldStrategyJavaFile.readString().contains("world_0_0"));
            final FSResource testPackageJavaFile = strategoJavaSourceFilePackageOutputDir.appendRelativePath("test.java");
            assertTrue(testPackageJavaFile.exists());
            assertTrue(testPackageJavaFile.isFile());
            assertTrue(testPackageJavaFile.readString().contains("test"));

            // Compile Java source files to Java class files.
            final CompileJava.Input.Builder inputBuilder = CompileJava.Input.builder()
                .sources(CompileJava.Sources.builder()
                    .addAllSourceFiles(result.get().javaSourceFiles)
                    .addSourcePaths(strategoJavaClassFileOutputDir.getPath())
                    .build()
                )
                .addClassPathSuppliers(new ValueSupplier<>(ListView.copyOf(javaClassPaths)))
                .release("8");
            final @Nullable String classPathProperty = System.getProperty("classPath");
            assertNotNull(classPathProperty);
            inputBuilder.addClassPathSuppliers(new ValueSupplier<>(ListView.of(Arrays.stream(classPathProperty.split(File.pathSeparator)).map(File::new).collect(Collectors.toList()))));
            final FSResource sourceFileOutputDirectory = directory(buildDir, "generated/sources/annotationProcessor/java/main");
            inputBuilder.sourceFileOutputDirectory(sourceFileOutputDirectory.getPath());
            final FSResource classFileOutputDirectory = directory(buildDir, "classes/java/main");
            inputBuilder.classFileOutputDirectory(classFileOutputDirectory.getPath());
            inputBuilder.addOriginTasks(strategoCompileTask.toSupplier());

            final Task<KeyedMessages> compileJavaTask = compileJava.createTask(inputBuilder.build());
            final KeyedMessages compileJavaResult = session.require(compileJavaTask);
            assertNoErrors(compileJavaResult);

            // Create a JAR from Java class files.
            final FSResource libsDir = directory(buildDir, "libs");
            final FSResource jarFile = libsDir.appendRelativePath("stratego.jar").ensureFileExists();
            final Task<?> createJarTask = archiveToJar.createTask(new ArchiveToJar.Input(
                null,
                list(ArchiveDirectory.ofClassFilesInDirectory(classFileOutputDirectory.getPath())),
                jarFile.getPath(),
                list(compileJavaTask.toSupplier())
            ));
            session.require(createJarTask);

            // Run the `hello` strategy from the compiled JAR.
            final StrategoRuntimeBuilder strategoRuntimeBuilder = new StrategoRuntimeBuilder(loggerFactory, resourceServiceComponent.getResourceService(), rootDirectory);
            strategoRuntimeBuilder.withJarParentClassLoader(getClass().getClassLoader());
            strategoRuntimeBuilder.addJar(jarFile.getURI().toURL());
            final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.build();
            final @Nullable IStrategoTerm strategoInvokeResult = strategoRuntime.invoke("hello", strategoRuntime.getTermFactory().makeTuple());
            assertNotNull(strategoInvokeResult);
            assertTrue(strategoInvokeResult instanceof IStrategoString);
            assertEquals("Hello, world!", ((IStrategoString)strategoInvokeResult).stringValue());
        }
    }

    private static class ToStratego2LibInfo extends StatelessSerializableFunction<StrategoLibInfo, Stratego2LibInfo> {
        @Override public Stratego2LibInfo apply(StrategoLibInfo strategoLibInfo) {
            return new Stratego2LibInfo(strategoLibInfo.str2libFile, strategoLibInfo.jarFilesOrDirectories);
        }
    }
}
