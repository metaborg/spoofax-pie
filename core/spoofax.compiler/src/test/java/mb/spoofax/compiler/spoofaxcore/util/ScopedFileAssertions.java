package mb.spoofax.compiler.spoofaxcore.util;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.TypeInfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public class ScopedFileAssertions {
    private final FileAssertions fileAssertions;
    private final ResourcePath dir;

    public ScopedFileAssertions(FileAssertions fileAssertions, ResourcePath dir) {
        this.fileAssertions = fileAssertions;
        this.dir = dir;
    }


    public void asserts(TypeInfo typeInfo, Consumer<FileAssertion> consumer) {
        final ResourcePath filePath = typeInfo.file(dir);
        try {
            fileAssertions.asserts(filePath, consumer);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public void assertNotExists(TypeInfo typeInfo) {
        asserts(typeInfo, FileAssertion::assertNotExists);
    }


    public void assertPublicJavaClass(TypeInfo typeInfo, String className) {
        asserts(typeInfo, (a) -> a.assertPublicJavaClass(className));
    }

    public void assertPublicJavaInterface(TypeInfo typeInfo, String className) {
        asserts(typeInfo, (a) -> a.assertPublicJavaInterface(className));
    }
}
