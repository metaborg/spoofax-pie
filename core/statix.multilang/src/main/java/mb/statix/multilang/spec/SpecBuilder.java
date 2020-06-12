package mb.statix.multilang.spec;

import mb.statix.spec.Spec;
import org.immutables.value.Value;

import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
public interface SpecBuilder {
    @Value.Parameter Set<Module> modules();

    @Value.Check default void checkModules() throws SpecLoadException {
        if (modules().isEmpty()) {
            throw new SpecLoadException("Cannot build Spec without any modules provided");
        }
    }

    @Value.Lazy default Spec toSpec() {
        return modules()
            .stream()
            .collect(Collectors.groupingBy(Module::moduleName))
            .values()
            .stream()
            // From all modules with the same name, just pick the first one
            // For now, users are responsible to ensure their languages use the same interfaces
            .map(modules -> modules.get(0))
            .map(Module::toSpec)
            .reduce(SpecUtils::mergeSpecs)
            // Safe, since check enforces at least one module to be present
            .orElseThrow(() -> new SpecLoadException("May not occur: at least one module should be present"));
    }

    default ImmutableSpecBuilder.Builder extend() {
        return ImmutableSpecBuilder.builder()
            .addAllModules(modules());
    }

    default SpecBuilder merge(SpecBuilder other) {
        return this.extend()
            .addAllModules(other.modules())
            .build();
    }
}
