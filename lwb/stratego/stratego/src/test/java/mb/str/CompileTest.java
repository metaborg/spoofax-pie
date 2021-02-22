package mb.str;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.MixedSession;
import mb.pie.api.None;
import mb.pie.api.Task;
import mb.pie.task.archive.ArchiveDirectory;
import mb.pie.task.archive.ArchiveToJar;
import mb.pie.task.java.CompileJava;
import mb.resource.fs.FSResource;
import mb.str.config.StrategoCompileConfig;
import mb.str.task.StrategoCompileToJava;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CompileTest extends TestBase {
    @Test void testCompileAndRun() throws Exception {
        final FSResource strategoSourceDir = directory(rootDirectory, "str");
        final FSResource strategoMainFile = textFile(strategoSourceDir, "hello.str", "" +
                "module hello " +
                "imports " +
                "  libstratego-lib " +
                "  world " +
                "rules " +
                "  hello = !$[Hello, [<world>]]; debug"
        );
        final FSResource strategoWorldFile = textFile(strategoSourceDir, "world.str", "" +
                "module world " +
                "imports " +
                "  libstratego-lib " +
                "rules " +
                "  world = !\"world!\""
        );

        final FSResource buildDir = directory(rootDirectory, "build");

        final FSResource strategoJavaOutputDir = directory(buildDir, "str/src/main/java/");
        final FSResource strategoJavaPackageOutputDir = directory(strategoJavaOutputDir, "mb/test");

        try(final MixedSession session = newSession()) {
            // Compile Stratego source files to Java source files.
            final StrategoCompileConfig config = new StrategoCompileConfig(
                strategoSourceDir.getPath(),
                strategoMainFile.getPath(),
                ListView.of(strategoSourceDir.getPath()),
                ListView.of("stratego-lib"),
                null,
                strategoJavaPackageOutputDir.getPath(),
                "mb.test"
            );
            final Task<Result<None, ?>> strategoCompileTask = compile.createTask(new StrategoCompileToJava.Input(
                config,
                list()
            ));
            @SuppressWarnings("ConstantConditions") final Result<None, ?> result = session.require(strategoCompileTask);
            assertTrue(result.isOk());
            assertTrue(strategoJavaPackageOutputDir.exists());
            assertTrue(strategoJavaPackageOutputDir.isDirectory());
            final FSResource interopRegistererJavaFile = strategoJavaPackageOutputDir.appendRelativePath("InteropRegisterer.java");
            assertTrue(interopRegistererJavaFile.exists());
            assertTrue(interopRegistererJavaFile.isFile());
            assertTrue(interopRegistererJavaFile.readString().contains("InteropRegisterer"));
            final FSResource mainJavaFile = strategoJavaPackageOutputDir.appendRelativePath("Main.java");
            assertTrue(mainJavaFile.exists());
            assertTrue(mainJavaFile.isFile());
            assertTrue(mainJavaFile.readString().contains("Main"));
            final FSResource mainStrategyJavaFile = strategoJavaPackageOutputDir.appendRelativePath("hello_0_0.java");
            assertTrue(mainStrategyJavaFile.exists());
            assertTrue(mainStrategyJavaFile.isFile());
            assertTrue(mainStrategyJavaFile.readString().contains("hello_0_0"));
            final FSResource worldStrategyJavaFile = strategoJavaPackageOutputDir.appendRelativePath("world_0_0.java");
            assertTrue(worldStrategyJavaFile.exists());
            assertTrue(worldStrategyJavaFile.isFile());
            assertTrue(worldStrategyJavaFile.readString().contains("world_0_0"));
            final FSResource testPackageJavaFile = strategoJavaPackageOutputDir.appendRelativePath("test.java");
            assertTrue(testPackageJavaFile.exists());
            assertTrue(testPackageJavaFile.isFile());
            assertTrue(testPackageJavaFile.readString().contains("test"));

            // Compile Java source files to Java class files.
            final FSResource sourceFileOutputDir = directory(buildDir, "generated/sources/annotationProcessor/java/main");
            final FSResource classFileOutputDir = directory(buildDir, "classes/java/main");
            final FSResource libsDir = directory(buildDir, "libs");
            final @Nullable String classPathProperty = System.getProperty("classPath");
            assertNotNull(classPathProperty);
            final ArrayList<File> classPath = list();
            for(String classPathPart : classPathProperty.split(File.pathSeparator)) {
                classPath.add(new File(classPathPart));
            }
            final Task<?> compileJavaTask = compileJava.createTask(new CompileJava.Input(
                list(mainJavaFile.getPath()),
                list(strategoJavaOutputDir.getPath()),
                classPath,
                list(),
                null,
                null,
                sourceFileOutputDir.getPath(),
                classFileOutputDir.getPath(),
                list(strategoCompileTask.toSupplier())
            ));
            session.require(compileJavaTask);

            // Create a JAR from Java class files.
            final FSResource jarFile = libsDir.appendRelativePath("stratego.jar").ensureFileExists();
            final Task<?> createJarTask = archiveToJar.createTask(new ArchiveToJar.Input(
                null,
                list(ArchiveDirectory.ofClassFilesInDirectory(classFileOutputDir.getPath())),
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
}
