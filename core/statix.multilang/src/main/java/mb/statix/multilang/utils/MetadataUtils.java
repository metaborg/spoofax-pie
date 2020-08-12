package mb.statix.multilang.utils;

import mb.pie.api.Function;
import mb.resource.ResourceKey;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.match.path.NoHiddenPathMatcher;
import mb.resource.hierarchical.walk.PathResourceWalker;
import mb.statix.multilang.metadata.spec.SpecBuilder;
import mb.statix.multilang.metadata.spec.SpecLoadException;
import mb.statix.multilang.metadata.spec.SpecUtils;
import org.spoofax.interpreter.terms.ITermFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.stream.Collectors;

public class MetadataUtils {

    private MetadataUtils() {
    }

    public static SpecBuilder loadSpec(ClassLoaderResource definitionDir, ITermFactory termFactory, String initialModule) {
        ClassLoaderResource specRoot = definitionDir.appendRelativePath("src-gen/statix");
        try {
            return SpecUtils.loadSpec(specRoot, initialModule, termFactory);
        } catch(SpecLoadException e) {
            throw new RuntimeException(e);
        }
    }

    public static Function<ResourcePath, HashSet<ResourceKey>> resourcesSupplierForExtensions(String... extensions) {
        return (exec, projectDir) -> {
            HierarchicalResource res = exec.getHierarchicalResource(projectDir);
            try {
                return res.walk(
                    new PathResourceWalker(new NoHiddenPathMatcher()),
                    new PathResourceMatcher(new ExtensionsPathMatcher(extensions)))
                    .map(HierarchicalResource::getKey)
                    .collect(Collectors.toCollection(HashSet::new));
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
