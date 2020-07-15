package mb.statix.multilang.spec;

import mb.common.result.Result;
import mb.common.result.ResultCollector;
import mb.statix.spec.Spec;
import org.immutables.value.Value;

import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
public interface SpecBuilder {
    @Value.Parameter Set<Module> modules();

    @Value.Check default void checkModules() {
        if(modules().isEmpty()) {
            throw new IllegalStateException("Cannot build Spec without any modules provided");
        }
    }

    @Value.Lazy default Spec toSpec() throws SpecLoadException {
        return toSpecResult().unwrap();
    }

    @Value.Lazy default Result<Spec, SpecLoadException> toSpecResult() {
        return modules()
            .stream()
            .collect(Collectors.groupingBy(Module::moduleName))
            .values()
            .stream()
            // From all modules with the same name, just pick the first one
            // For now, users are responsible to ensure their languages use the same interfaces
            .map(modules -> modules.get(0))
            .map(Module::toSpecResult)
            .collect(ResultCollector.getWithBaseException(new SpecLoadException("Error mapping module to spec", false)))
            .flatMap(specs -> specs.stream()
                .map(Result::<Spec, SpecLoadException>ofOk)
                .reduce(SpecUtils::mergeSpecs)
                // When Check method holds, this can not occur
                .orElse(Result.ofErr(new SpecLoadException("Bug: check should ensure at least one module should be present"))));
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
