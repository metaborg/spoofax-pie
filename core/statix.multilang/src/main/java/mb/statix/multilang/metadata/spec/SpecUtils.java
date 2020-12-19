package mb.statix.multilang.metadata.spec;

import com.google.common.collect.Multimap;
import mb.common.result.Result;
import mb.common.result.ResultCollector;
import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.IStringTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.Terms;
import mb.nabl2.terms.matching.TermMatch.IMatcher;
import mb.nabl2.util.Tuple2;
import mb.pie.api.Supplier;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.SpecManager;
import mb.statix.spec.Rule;
import mb.statix.spec.RuleSet;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;
import org.immutables.value.Value;
import org.metaborg.util.functions.Function1;
import org.metaborg.util.functions.Function4;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

@Value.Enclosing
public class SpecUtils {
    private static final String LABEL_OP = "Label";

    // Spec loading utils

    public static IMatcher<Spec> fileSpec() {
        return fileSpec((edgeLabels, dataLabels, rules, ext) -> Spec.of(rules, edgeLabels, dataLabels, ext));
    }

    public static Stream<String> allCustomLabels(ITerm module) {
        return fileSpec((edgeLabels, dataLabels, rules, ext) -> Stream.concat(edgeLabels.stream(), dataLabels.stream())
            .map(M.appl1(LABEL_OP, M.stringValue(), (a, s) -> s)::match)
            .filter(Optional::isPresent)
            .map(Optional::get))
            .match(module)
            .orElse(Stream.empty());
    }

    public static <T> IMatcher<T> fileSpec(Function4<List<ITerm>, List<ITerm>, RuleSet, Multimap<String, Tuple2<Integer, ITerm>>, T> f) {
        return M.appl6("FileSpec", M.list(), M.req(StatixTerms.labels()), M.req(StatixTerms.labels()), M.term(), StatixTerms.rules(), M.req(StatixTerms.scopeExtensions()),
            (t, l, edgeLabels, dataLabels, noRelationLabel, rules, ext) -> f.apply(edgeLabels, dataLabels, rules, ext));
    }

    public static Result<Set<SpecFragmentId>, SpecLoadException> getRequiredFragments(SpecFragmentId specFragmentId, SpecManager specManager) {
        return specManager.getSpecConfig(specFragmentId).flatMap(config -> config.dependencies().stream()
            .map(id -> getRequiredFragments(id, specManager))
            .collect(ResultCollector.getWithBaseException(new SpecLoadException("BUG: Error computing spec dependencies for " + specFragmentId)))
            .map(sets -> {
                final HashSet<SpecFragmentId> result = new HashSet<>(Collections.singleton(specFragmentId));
                sets.forEach(result::addAll);
                return result;
            }));
    }

    // Spec mergin utils

    public static Result<Spec, SpecLoadException> mergeSpecs(Result<Spec, SpecLoadException> accResult, Result<Spec, SpecLoadException> newSpecResult) {
        return accResult.mapOrElse(acc -> newSpecResult.mapOrElse(newSpec -> Result.ofOk(mergeSpecs(acc, newSpec)), Result::ofErr),
            accErr -> newSpecResult.mapOrElse(
                res -> Result.ofErr(accErr),
                err -> {
                    accErr.addSuppressed(err);
                    return Result.ofErr(accErr);
                }
            ));
    }

    public static Spec mergeSpecs(Spec acc, Spec newSpec) {
        Set<Rule> rules = new HashSet<>(acc.rules().getAllRules());
        rules.addAll(newSpec.rules().getAllRules());

        return Spec.builder()
            .from(acc)
            .rules(RuleSet.of(rules))
            .addAllEdgeLabels(newSpec.edgeLabels())
            .addAllDataLabels(newSpec.dataLabels())
            .putAllScopeExtensions(newSpec.scopeExtensions())
            .build();
    }

    // Stream utils

    public static <K, V> Map.Entry<K, V> pair(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K,V>> toMap() {
        return toMap(HashMap::new);
    }

    public static <K, V, M extends Map<K, V>> Collector<Map.Entry<K, V>, ?, M> toMap(java.util.function.Supplier<M> mapSupplier) {
        return Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (p, n) -> n,
            mapSupplier
        );
    }

    // Utility class to rename

    @Value.Immutable
    abstract static class LabelRenamer {

        @Value.Parameter public abstract Function1<String, String> renameFunc();

        public ITerm renameTerm(ITerm term) {
            return term.match(Terms.casesFix(
                (m, appl) -> label().match(appl)
                    .orElseGet(() -> B.newAppl(appl.getOp(), appl.getArgs()
                        .stream()
                        .map(a -> a.match(m))
                        .collect(Collectors.toList()), appl.getAttachments())),
                (m, list) -> list.match(ListTerms.<IListTerm>casesFix(
                    (lm, cons) -> B.newCons(cons.getHead().match(m), cons.getTail().match(lm), cons.getAttachments()),
                    (lm, nil) -> nil,
                    (lm, var) -> var
                )),
                (m, string) -> string,
                (m, integer) -> integer,
                (m, blob) -> blob,
                (m, var) -> var
            ));
        }

        private IMatcher<ITerm> label() {
            return M.appl1(LABEL_OP, M.stringValue(), this::renameLabel);
        }

        private ITerm renameLabel(IApplTerm appl, String lbl) {
            IStringTerm newLabel = B.newString(renameFunc().apply(lbl));
            return B.newAppl(LABEL_OP, Collections.singletonList(newLabel), appl.getAttachments());
        }

        public static LabelRenamer forRenameFunc(Function1<String, String> renameFunc) {
            return ImmutableSpecUtils.LabelRenamer.of(renameFunc);
        }
    }
}
