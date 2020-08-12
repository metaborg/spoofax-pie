package mb.statix.multilang.metadata.spec;

import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.statix.spec.Spec;
import org.immutables.value.Value;

@Value.Immutable
public interface Module {
    @Value.Parameter String moduleName();

    @Value.Parameter ITerm module();

    @Value.Lazy default Spec toSpec() throws SpecLoadException {
        return toSpecResult().unwrap();
    }

    @Value.Lazy default Result<Spec, SpecLoadException> toSpecResult() {
        return SpecUtils.fileSpec()
            .match(module())
            .map(Result::<Spec, SpecLoadException>ofOk)
            .orElse(Result.ofErr(new SpecLoadException(String.format("Module %s does not contain a valid Statix spec", moduleName()))));
    }
}
