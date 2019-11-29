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

import static org.junit.jupiter.api.Assertions.*;

public class FileAssertions {
    private final HierarchicalResource resource;
    private @Nullable String string;

    public FileAssertions(HierarchicalResource resource) {
        this.resource = resource;
    }

    public FileAssertions(ResourcePath path, ResourceService resourceService) {
        this.resource = resourceService.getHierarchicalResource(path);
    }


    public void assertName(String expected) {
        assertEquals(expected, resource.getLeaf());
    }

    public void assertExists() throws IOException {
        assertTrue(resource.exists());
    }

    public void assertNotExists() throws IOException {
        assertFalse(resource.exists());
    }

    public void assertContains(String s) throws IOException {
        assertTrue(readString().contains(s));
    }

    public void assertJavaParses(JavaParser parser) throws IOException {
        parser.assertParses(readString());
    }

    public void assertGradleBuild(String... tasks) {
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
                .setStandardOutput(System.out).setStandardError(System.err) // Redirect standard out and err.
                .run();
        } catch(Throwable e) {
            throw e; // Place breakpoint here to debug failures.
        }
    }


    private String readString() throws IOException {
        if(string == null) {
            string = resource.readString();
        }
        return string;
    }
}
