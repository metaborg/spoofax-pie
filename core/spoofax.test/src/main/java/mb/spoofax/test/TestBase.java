package mb.spoofax.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.ExceptionPrinter;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.ResourceStringSupplier;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.text.TextResource;
import mb.resource.text.TextResourceRegistry;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class TestBase {
    public final ExceptionPrinter exceptionPrinter = new ExceptionPrinter();

    public final LoggerComponent loggerComponent;
    public final LoggerFactory loggerFactory;
    public final Logger log;

    public final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    public final FSResource rootDirectory = new FSResource(fileSystem.getPath("/"));
    public final ResourcePath rootPath = rootDirectory.getPath();
    public final TextResourceRegistry textResourceRegistry = new TextResourceRegistry();


    public TestBase(LoggerComponent loggerComponent) {
        this.loggerComponent = loggerComponent;
        loggerFactory = loggerComponent.getLoggerFactory();
        log = loggerFactory.create(getClass());
    }


    @SafeVarargs protected final <T> ArrayList<T> list(T... items) {
        final ArrayList<T> list = new ArrayList<>();
        Collections.addAll(list, items);
        return list;
    }


    public FSResource resource(FSResource parent, String relativePath) throws IOException {
        return parent.appendRelativePath(relativePath);
    }

    public FSResource resource(String relativePath) throws IOException {
        return resource(rootDirectory, relativePath);
    }

    public FSResource directory(FSResource parent, String relativePath) throws IOException {
        return resource(parent, relativePath).ensureDirectoryExists();
    }

    public FSResource directory(String relativePath) throws IOException {
        return directory(rootDirectory, relativePath);
    }

    public FSResource file(FSResource parent, String relativePath) throws IOException {
        return resource(parent, relativePath).ensureFileExists();
    }

    public FSResource file(String relativePath) throws IOException {
        return file(rootDirectory, relativePath);
    }

    public FSResource textFile(FSResource parent, String relativePath, String text) throws IOException {
        final FSResource file = file(parent, relativePath);
        return writeText(file, text);
    }

    public FSResource textFile(String relativePath, String text) throws IOException {
        return textFile(rootDirectory, relativePath, text);
    }


    public FSResource writeText(FSResource file, String text) throws IOException {
        file.writeString(text, StandardCharsets.UTF_8);
        return file;
    }


    public TextResource textResource(String id, String text) {
        return textResourceRegistry.createResource(text, id);
    }


    public ResourceStringSupplier resourceStringSupplier(ResourceKey resourceKey) {
        return new ResourceStringSupplier(resourceKey);
    }

    public ResourceStringSupplier resourceStringSupplier(Resource resource) {
        return resourceStringSupplier(resource.getKey());
    }


    public void assertNoErrors(KeyedMessages messages) {
        assertNoErrors(messages, "no errors, but found errors");
    }

    public void assertNoErrors(KeyedMessages messages, String failure) {
        assertFalse(messages.containsError(), () -> "Expected " + failure + ".\n" + exceptionPrinter.printMessagesToString(messages));
    }

    public void assertErrors(KeyedMessages messages) {
        assertNoErrors(messages, "errors, but found no errors");
    }

    public void assertErrors(KeyedMessages messages, String failure) {
        assertTrue(messages.containsError(), () -> "Expected " + failure + ".\n" + exceptionPrinter.printMessagesToString(messages));
    }

    public <T, E extends Exception> void assertOk(Result<T, E> result) {
        // noinspection ConstantConditions (err is present)
        assertTrue(result.isOk(), () -> "Expected Ok Result, but found Err.\n" + exceptionPrinter.printExceptionToString(result.getErr()));
    }

    public <T, E extends Exception> void assertErr(Result<T, E> result) {
        assertTrue(result.isErr(), () -> "Expected Err Result, but found Ok: " + result);
    }


    @AfterEach void closeFileSystem() throws IOException {
        if(fileSystem.isOpen()) fileSystem.close();
    }
}
