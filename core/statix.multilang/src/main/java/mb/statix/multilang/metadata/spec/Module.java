package mb.statix.multilang.metadata.spec;

import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.statix.spec.Spec;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface Module {
    @Value.Parameter String moduleName();

    @Value.Parameter ITerm module();

    @Value.Lazy default Result<Spec, SpecLoadException> toSpecResult() {
        // One intermediate variable needed to work around type erase quirk when type checking
        Optional<Result<Spec, SpecLoadException>> optionalSpecResult = SpecUtils.fileSpec().match(module()).map(Result::ofOk);
        return optionalSpecResult.orElse(Result.ofErr(new SpecLoadException(String.format("Module %s does not contain a valid Statix spec", moduleName()))));
    }
}
