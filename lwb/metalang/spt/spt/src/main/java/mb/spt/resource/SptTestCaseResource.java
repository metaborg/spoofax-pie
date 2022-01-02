package mb.spt.resource;

import mb.common.text.Text;
import mb.common.util.ListView;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.HierarchicalResourceDefaults;
import mb.resource.hierarchical.HierarchicalResourceType;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.core.resource.TextResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Stream;

public class SptTestCaseResource extends HierarchicalResourceDefaults<SptTestCaseResource> implements TextResource {
    private final SptTestCaseResourcePath key;
    private final Text text;
    private final Instant modified;

    public SptTestCaseResource(SptTestCaseResourcePath key, Text text) {
        this.key = key;
        this.text = text;
        this.modified = Instant.now();
    }

    @Override public void close() {
        // Nothing to close.
    }


    @Override public Text getText() {
        return text;
    }


    @Override public SptTestCaseResourcePath getKey() {
        return key;
    }

    @Override public boolean exists() {
        return true; // Always exists.
    }

    @Override public boolean isReadable() {
        return true; // Always readable.
    }

    @Override public Instant getLastModifiedTime() {
        return modified;
    }

    @Override public long getSize() {
        return text.toString().length() * 2L; // UTF-16 is 2 bytes per character.
    }

    @Override
    public InputStream openRead() {
        return new ByteArrayInputStream(readBytes());
    }

    @Override
    public byte[] readBytes() {
        return text.toString().getBytes(StandardCharsets.UTF_8); // Encode as UTF-8 bytes.
    }

    @Override
    public String readString(Charset fromCharset) {
        return text.toString(); // Ignore the character set, we do not need to decode from bytes.
    }


    @Override protected SptTestCaseResource self() {
        return this;
    }

    @Override public @Nullable HierarchicalResource getParent() {
        return null;
    }

    @Override public @Nullable HierarchicalResource getRoot() {
        return null;
    }

    @Override public HierarchicalResource getNormalized() {
        return this;
    }

    @Override public HierarchicalResource appendSegment(String segment) {
        return this;
    }

    @Override public HierarchicalResource appendOrReplaceWithPath(String other) {
        return this;
    }

    @Override public HierarchicalResourceType getType() throws IOException {
        return HierarchicalResourceType.Directory; // Mock as directory to support list/walk.
    }

    @Override public void copyTo(HierarchicalResource other) throws IOException {

    }

    @Override public void copyRecursivelyTo(HierarchicalResource other) throws IOException {

    }

    @Override public void moveTo(HierarchicalResource other) throws IOException {

    }

    @Override public HierarchicalResource createParents() throws IOException {
        return this;
    }

    @Override public void delete(boolean deleteRecursively) throws IOException {

    }

    @Override public boolean isWritable() throws IOException {
        return false;
    }

    @Override public void setLastModifiedTime(Instant moment) throws IOException {

    }

    @Override public OutputStream openWriteAppend() throws IOException {
        return new ByteArrayOutputStream();
    }

    @Override public OutputStream openWriteExisting() throws IOException {
        return new ByteArrayOutputStream();
    }

    @Override public SptTestCaseResource appendSegments(Iterable<String> segments) {
        return this;
    }

    @Override public SptTestCaseResource appendRelativePath(String relativePath) {
        return this;
    }

    @Override public SptTestCaseResource appendString(String other) {
        return this;
    }

    @Override public SptTestCaseResource appendRelativePath(ResourcePath relativePath) {
        return this;
    }

    @Override public SptTestCaseResource replaceLeaf(String segment) {
        return this;
    }

    @Override public Stream<SptTestCaseResource> list(ResourceMatcher matcher) throws IOException {
        return ListView.of(this).stream();
    }

    @Override
    public Stream<SptTestCaseResource> walk(ResourceWalker walker, ResourceMatcher matcher) throws IOException {
        return ListView.of(this).stream();
    }

    @Override public SptTestCaseResource createFile(boolean createParents) throws IOException {
        return this;
    }

    @Override public SptTestCaseResource createDirectory(boolean createParents) throws IOException {
        return this;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SptTestCaseResource that = (SptTestCaseResource)o;
        if(!key.equals(that.key)) return false;
        return text.equals(that.text);
    }

    @Override public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }

    @Override public String toString() {
        return key.toString();
    }
}
