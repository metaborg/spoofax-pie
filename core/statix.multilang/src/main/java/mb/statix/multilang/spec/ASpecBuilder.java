package mb.statix.multilang.spec;

import mb.statix.spec.Spec;
import org.immutables.value.Value;

import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
public interface ASpecBuilder {
    @Value.Parameter Set<Module> modules();

    @Value.Check default void checkModules() throws SpecLoadException {
        if (modules().isEmpty()) {
            throw new SpecLoadException("Cannot build Spec without any modules provided");
        }

        // Checking that when a module is included multiple times,
        // it is included in the exact same variant.
        // Otherwise it is not possible to determine which variant to include
        // while including both results in rule duplication (invalid Specs)
        Set<String> invalidDuplicates = modules()
            .stream()
            .collect(Collectors.groupingBy(Module::moduleName))
            .values()
            .stream()
            .filter(mods -> mods.size() > 1)
            // There is no need for checking if the module terms differ
            // since when a module has both the same name and AST, it would have been included
            // only once in the initial modules set.
            // Now that there are more modules with the same name, we can be sure their contents differ.
            .map(mods -> mods.get(0))
            .map(Module::moduleName)
            .collect(Collectors.toSet());

        if (!invalidDuplicates.isEmpty()) {
            throw new SpecLoadException(
                String.format("The following modules are included in different variants: %s. " +
                    "Be sure that all the languages you use depend on the same shared interface.",
                    String.join(", ", invalidDuplicates)));
        }
    }

    @Value.Lazy default Spec toSpec() {
        return modules()
            .stream()
            .map(Module::toSpec)
            .reduce(SpecUtils::mergeSpecs)
            // Safe, since check enforces at least one module to be present
            .orElseThrow(() -> new SpecLoadException("May not occur: at least one module should be present"));
    }

    default SpecBuilder.Builder extend() {
        return SpecBuilder.builder()
            .addAllModules(modules());
    }
}
