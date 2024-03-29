package mb.tiger.spoofax.task;

import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.AllResourceMatcher;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionPathMatcher;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import mb.tiger.TigerClassloaderResources;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

@TigerScope
public class TigerGetStrategoRuntimeProvider extends GetStrategoRuntimeProvider {
    private final TigerClassloaderResources classLoaderResources;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject
    public TigerGetStrategoRuntimeProvider(
        TigerClassloaderResources classLoaderResources,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.classLoaderResources = classLoaderResources;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return "mb.tiger.task.TigerGetStrategoRuntimeProvider";
    }

    @Override protected Provider<StrategoRuntime> getStrategoRuntimeProvider(ExecContext context) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        try {
            classLoaderResources.performWithResourceLocations(
            "tiger/spoofaxcore/trans",
                directory -> {
                    try(final Stream<? extends HierarchicalResource> stream = directory.walk(
                        new AllResourceMatcher(new FileResourceMatcher(), new PathResourceMatcher(new ExtensionPathMatcher("class")))
                    )) {
                        stream.forEach(resource -> {
                            try {
                                context.require(resource, ResourceStampers.hashFile());
                            } catch(IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                    }
                },
                jarFileWithPath -> context.require(jarFileWithPath.file)
            );
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
        return strategoRuntimeProvider;
    }
}
