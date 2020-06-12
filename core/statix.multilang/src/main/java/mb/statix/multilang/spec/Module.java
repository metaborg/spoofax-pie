package mb.statix.multilang.spec;

import mb.nabl2.terms.ITerm;
import mb.statix.spec.Spec;
import org.immutables.value.Value;

@Value.Immutable
public interface Module {
    @Value.Parameter String moduleName();

    @Value.Parameter ITerm module();

    @Value.Lazy default Spec toSpec() {
        return SpecUtils.fileSpec()
            .match(module())
            .orElseThrow(() -> new SpecLoadException(
                String.format("Module %s does not contain a valid Statix spec", moduleName())));
    }
}
