package mb.spt.resource;

import mb.common.text.Text;
import mb.common.util.MultiMap;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceRuntimeException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;

public class SptTestCaseResourceRegistry implements ResourceRegistry, AutoCloseable {
    public static final String qualifier = "spt";

    private final HashMap<String, SptTestCaseResource> identifierToResource = new HashMap<>();
    private final MultiMap<String, String> testSuiteToIdentifiers = MultiMap.withHash();


    @Override public void close() {
        identifierToResource.clear();
        testSuiteToIdentifiers.clear();
    }


    @Override public String qualifier() {
        return qualifier;
    }

    @Override public SptTestCaseResourcePath getResourceKey(ResourceKeyString keyStr) {
        if(!keyStr.qualifierMatchesOrMissing(qualifier)) {
            throw new ResourceRuntimeException("Qualifier of '" + keyStr + "' does not match qualifier '" + qualifier + "' of this resource registry");
        }
        return new SptTestCaseResourcePath(keyStr.getId());
    }

    @Override public SptTestCaseResource getResource(ResourceKey key) {
        if(!(key instanceof SptTestCaseResourcePath)) {
            throw new ResourceRuntimeException("Cannot get SptTestCaseResource for key '" + key + "'; it is not of type SptTestCaseResourceKey");
        }
        return getResource(((SptTestCaseResourcePath)key).identifier);
    }

    @Override public SptTestCaseResource getResource(ResourceKeyString keyStr) {
        if(!keyStr.qualifierMatchesOrMissing(qualifier)) {
            throw new ResourceRuntimeException("Qualifier of '" + keyStr + "' does not match qualifier '" + qualifier + "' of this resource registry");
        }
        return getResource(keyStr.getId());
    }


    public SptTestCaseResource registerTestCase(ResourceKey testSuiteFile, String testCase, Text text) {
        final String testSuite = testSuiteFile.getIdAsString();
        final SptTestCaseResourcePath key = new SptTestCaseResourcePath(testSuiteFile.getIdAsString(), testCase);
        final SptTestCaseResource resource = new SptTestCaseResource(key, text);
        identifierToResource.put(key.identifier, resource);
        testSuiteToIdentifiers.put(testSuite, key.identifier);
        return resource;
    }

    public void clearTestSuite(ResourceKey testSuiteFile) {
        testSuiteToIdentifiers.removeAll(testSuiteFile.getIdAsString());
    }


    private SptTestCaseResource getResource(String identifier) {
        final @Nullable SptTestCaseResource resource = identifierToResource.get(identifier);
        if(resource == null) {
            throw new ResourceRuntimeException("Cannot get SPT test case resource for identifier '" + identifier + "'; no resource was registered for that identifier");
        }
        return resource;
    }
}
