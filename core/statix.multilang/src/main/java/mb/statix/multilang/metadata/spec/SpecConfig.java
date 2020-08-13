package mb.statix.multilang.metadata.spec;

import mb.common.result.Result;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.statix.multilang.metadata.SpecFragmentId;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.Set;

@Value.Immutable
public interface SpecConfig {

    @Value.Parameter HierarchicalResource rootPackage();

    @Value.Parameter Set<SpecFragmentId> dependencies();

    @Value.Parameter Set<String> rootModules();

    @Value.Parameter ITermFactory termFactory();

    @Value.Lazy default Result<SpecFragment, SpecLoadException> load() {
        try {
            return Result.ofOk(SpecUtils.loadSpec(rootPackage(), rootModules(), termFactory()));
        } catch(SpecLoadException e) {
            return Result.ofErr(e);
        }
    }
}
