package mb.statix.multilang.metadata.spec;

import mb.common.result.Result;
import mb.common.result.ResultCollector;
import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.IStringTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.matching.TermMatch.IMatcher;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.SpecManager;
import mb.statix.spec.Rule;
import mb.statix.spec.RuleSet;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;
import org.immutables.value.Value;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final String FILESPEC_OP = "FileSpec";
    private static final String LABEL_OP = "Label";
    private static final String RULE_OP = "Rule";
    private static final String DECL_OP = "Decl";
    private static final String CONSTRAINT_OP = "C";

    // Spec loading utils

    public static IMatcher<Spec> fileSpec() {
        return M.appl6(FILESPEC_OP, M.list(), M.req(StatixTerms.labels()), M.req(StatixTerms.labels()), M.term(), StatixTerms.rules(), M.req(StatixTerms.scopeExtensions()),
            (t, l, edgeLabels, dataLabels, noRelationLabel, rules, ext) -> Spec.of(rules, edgeLabels, dataLabels, ext));
    }

    public static Stream<String> allCustomLabels(ITerm module) {
        return M.appl6(FILESPEC_OP, M.list(), M.req(StatixTerms.labels()), M.req(StatixTerms.labels()), M.term(), M.list(), M.list(),
            (appl, i, edgeLabels, dataLabels, n, c, t) ->  Stream.concat(edgeLabels.stream(), dataLabels.stream())
                .map(M.appl1(LABEL_OP, M.stringValue(), (a, s) -> s)::match)
                .filter(Optional::isPresent)
                .map(Optional::get))
            .match(module)
            .orElse(Stream.empty());
    }

    public static Stream<String> allConstraints(ITerm module) {
        return M.appl6(FILESPEC_OP, M.list(), M.list(), M.list(), M.term(), M.listElems(rule()), M.list(), (a, i, e, r, n, c, t) -> c.stream().distinct())
            .match(module)
            .orElse(Stream.empty());
    }

    private static IMatcher<String> rule() {
        return M.appl3(RULE_OP, M.term(), ruleHead(), M.term(), (a, h, s, b) -> s);
    }

    private static IMatcher<String> ruleHead() {
        return M.appl2(CONSTRAINT_OP, M.stringValue(), M.list(), (a, s, p) -> s);
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

    // Spec merging utils

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

    public static ITerm qualifyFileSpec(ITerm spec, NameQualifier qualifier) {
        return M.preserveAttachments(M.appl6(
            FILESPEC_OP,
            M.term(),
            renameTerm(qualifier.label()),
            M.cases(renameTerm(qualifier.label()), M.appl0(DECL_OP)),
            M.term(),
            renameTerm(M.cases(qualifier.label(), qualifier.constraintName())),
            renameScopeExtension(qualifier),
            (a, i, e, d, n, r, s) -> (ITerm) B.newAppl(a.getOp(), i, e, d, n, r, s)
        )).match(spec).orElseThrow(() -> new IllegalArgumentException("spec"));
    }

    private static IMatcher<ITerm> renameScopeExtension(NameQualifier qualifier) {
        return M.listElems(
            M.preserveAttachments(M.tuple3(
                M.preserveAttachments(M.string(str -> B.newString(qualifier.renameConstraint(str.getValue())))),
                M.term(),
                M.cases(renameTerm(qualifier.label()), M.appl0(DECL_OP)),
                (a, c, p, l) -> B.newTuple(c, p, l))),
            (l, elems) -> B.newList(elems)
        );
    }

    public static IMatcher<ITerm> renameTerm(IMatcher<ITerm> renamingMatcher) {
        return M.casesFix(f -> Arrays.asList(
            renamingMatcher,
            M.preserveAttachments(M.appl(t -> (ITerm)B.newAppl(t.getOp(), t.getArgs().stream()
                .map(f::match)
                .map(Optional::get) // Base case M.term() ensures this is always safe
                .collect(Collectors.toList())))),
            M.preserveAttachments(M.listElems(f, (l, elems) -> B.newList(elems))),
            M.term() // Preserve atoms
        ));
    }

    // Utility class to rename

    public interface NameQualifier {
        String renameLabel(String baseName);
        String renameConstraint(String baseName);

        default IMatcher<ITerm> label() {
            return M.preserveAttachments(M.appl1(LABEL_OP, M.stringValue(), this::renameLabel));
        }

        default IMatcher<ITerm> constraintName() {
            return M.preserveAttachments(M.cases(
                // C/2 term in rule head
                M.appl2(CONSTRAINT_OP, M.stringValue(), M.term(), this::renameConstraint),
                // C/3 term in constraint reference. May contain message.
                M.appl3(CONSTRAINT_OP, M.stringValue(), M.term(), M.term(), this::renameConstraint)
            ));
        }

        default ITerm renameLabel(IApplTerm appl, String lbl) {
            IStringTerm newLabel = B.newString(renameLabel(lbl));
            return B.newAppl(LABEL_OP, newLabel);
        }

        default ITerm renameConstraint(IApplTerm appl, String lbl, ITerm params) {
            return B.newAppl(CONSTRAINT_OP, B.newString(renameConstraint(lbl)), params);
        }

        default ITerm renameConstraint(IApplTerm appl, String lbl, ITerm params, ITerm msg) {
            return B.newAppl(CONSTRAINT_OP, B.newString(renameConstraint(lbl)), params, msg);
        }

    }

    @Value.Immutable
    public static abstract class MapBasedNameQualifier implements NameQualifier {

        @Value.Parameter public abstract Map<String, String> labelRenames();

        @Value.Parameter public abstract Map<String, String> constraintRenames();

        @Override
        public String renameLabel(String baseName) {
            return labelRenames().getOrDefault(baseName, baseName);
        }

        @Override
        public String renameConstraint(String baseName) {
            return constraintRenames().getOrDefault(baseName, baseName);
        }

        public static MapBasedNameQualifier from(Map<String, String> labelRenames, Map<String, String> constraintRenames) {
            return ImmutableSpecUtils.MapBasedNameQualifier.of(labelRenames, constraintRenames);
        }
    }
}
