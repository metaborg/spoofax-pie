package mb.spoofax.compiler.spoofaxcore.util;

import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public class FileAssertions {
    private final ResourceService resourceService;
    private final JavaParser javaParser;

    public FileAssertions(ResourceService resourceService, JavaParser javaParser) {
        this.resourceService = resourceService;
        this.javaParser = javaParser;
    }

    public FileAssertions(ResourceService resourceService) {
        this(resourceService, new JavaParser());
    }

    public void asserts(ResourcePath filePath, Consumer<FileAssertion> consumer) throws IOException {
        final FileAssertion assertion = new FileAssertion(filePath, resourceService, javaParser);
        try {
            consumer.accept(assertion);
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }


    public void assertPublicJavaClass(ResourcePath filePath, String className) throws IOException {
        asserts(filePath, (a) -> a.assertPublicJavaClass(className));
    }

    public void assertPublicJavaInterface(ResourcePath filePath, String className) throws IOException {
        asserts(filePath, (a) -> a.assertPublicJavaInterface(className));
    }


    public void scopedExists(ResourcePath dir, Consumer<ScopedFileAssertions> consumer) throws IOException {
        asserts(dir, FileAssertion::assertExists);
        final ScopedFileAssertions assertions = new ScopedFileAssertions(this, dir);
        try {
            consumer.accept(assertions);
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }

    public void scopedNotExists(ResourcePath dir, Consumer<ScopedFileAssertions> consumer) throws IOException {
        asserts(dir, FileAssertion::assertNotExists);
        final ScopedFileAssertions assertions = new ScopedFileAssertions(this, dir);
        try {
            consumer.accept(assertions);
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
