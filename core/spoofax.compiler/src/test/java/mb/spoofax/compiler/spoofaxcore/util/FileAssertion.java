package mb.spoofax.compiler.spoofaxcore.util;

import mb.resource.ResourceService;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileAssertion {
    private final HierarchicalResource resource;
    private final JavaParser javaParser;

    private @Nullable String string;

    public FileAssertion(HierarchicalResource resource, JavaParser javaParser) {
        this.resource = resource;
        this.javaParser = javaParser;
    }

    public FileAssertion(ResourcePath path, ResourceService resourceService, JavaParser javaParser) {
        this.resource = resourceService.getHierarchicalResource(path);
        this.javaParser = javaParser;
    }


    public void assertExists() {
        try {
            assertTrue(resource.exists(), "Expected resource to exist at: " + resource.getKey().toString());
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void assertNotExists() {
        try {
            assertFalse(resource.exists());
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public void assertLeaf(String expected) {
        assertEquals(expected, resource.getLeaf());
    }


    public void assertContains(String expected) {
        assertExists();
        assertTrue(readString().contains(expected));
    }

    public void assertAll(String expectedLeaf, String expectedContent) {
        assertExists();
        assertLeaf(expectedLeaf);
        assertContains(expectedContent);
    }


    public void assertJavaParses() {
        assertExists();
        javaParser.assertParses(readString());
    }


    public void assertGradleBuild(String... tasks) {
        assertExists();
        final File projectDirectory = ((FSResource)resource).getJavaPath().toFile();
        // noinspection CaughtExceptionImmediatelyRethrown
        try(final ProjectConnection connection = GradleConnector.newConnector()
            .forProjectDirectory(projectDirectory)
            .connect()
        ) {
            //noinspection UnstableApiUsage
            connection.newBuild()
                .forTasks(tasks)
                .addArguments("--quiet") // Only print important information messages and errors.
                .addArguments("--stacktrace")
                .setStandardOutput(System.out).setStandardError(System.err) // Redirect standard out and err.
                .run();
        } catch(Throwable e) {
            throw e; // Place breakpoint here to debug failures.
        }
    }


    public void assertJava(String s) {
        assertExists();
        assertContains(s);
        assertJavaParses();
    }

    public void assertJavaAll(String expectedLeaf, String expectedContent) {
        assertExists();
        assertLeaf(expectedLeaf);
        assertContains(expectedContent);
        assertJavaParses();
    }

    public void assertPublicJavaClass(String className) {
        assertJavaAll(className + ".java", "public class " + className);
    }

    public void assertPublicJavaInterface(String interfaceName) {
        assertJavaAll(interfaceName + ".java", "public interface " + interfaceName);
    }


    private String readString() {
        if(string == null) {
            try {
                string = resource.readString();
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return string;
    }
}
