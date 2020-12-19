package mb.statix.multilang.metadata.spec;

import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.statix.spec.Spec;
import org.immutables.value.Value;
import org.metaborg.util.functions.Function1;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static mb.statix.multilang.metadata.spec.SpecUtils.pair;

@Value.Immutable
public interface Module {
    @Value.Parameter String moduleName();

    @Value.Parameter ITerm module();

    default Stream<Map.Entry<String, String>> qualifiedLabels(String prefix) {
        return SpecUtils.allCustomLabels(module())
            .map(label -> pair(label, String.format("%s:%s", prefix, label)));
    }

    default Result<Spec, SpecLoadException> load(Function1<String, String> renameFunc) {
        ITerm renamedModule = SpecUtils.LabelRenamer.forRenameFunc(renameFunc).renameTerm(module());
        Optional<Result<Spec, SpecLoadException>> optionalSpecResult = SpecUtils.fileSpec().match(renamedModule).map(Result::ofOk);
        return optionalSpecResult.orElse(Result.ofErr(new SpecLoadException(String.format("Module %s does not contain a valid Statix spec", moduleName()))));
    }
}
