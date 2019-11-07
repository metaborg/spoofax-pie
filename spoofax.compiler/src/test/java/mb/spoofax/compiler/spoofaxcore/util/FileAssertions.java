package mb.spoofax.compiler.spoofaxcore.util;

import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileAssertions {
    private final HierarchicalResource file;
    private @Nullable String string;

    public FileAssertions(HierarchicalResource file) {
        this.file = file;
    }


    public void assertName(String expected) {
        assertEquals(expected, file.getLeaf());
    }

    public void assertExists() throws IOException {
        assertTrue(file.exists());
    }

    public void assertContains(String s) throws IOException {
        assertTrue(readString().contains(s));
    }

    public void assertJavaParses(JavaParser parser) throws IOException {
        parser.assertParses(readString());
    }


    private String readString() throws IOException {
        if(string == null) {
            string = file.readString();
        }
        return string;
    }
}
