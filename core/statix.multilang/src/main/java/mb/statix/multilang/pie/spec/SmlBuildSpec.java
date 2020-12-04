package mb.statix.multilang.pie.spec;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import dagger.Lazy;
import mb.common.result.Result;
import mb.common.result.ResultCollector;
import mb.nabl2.terms.ITerm;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.SpecManager;
import mb.statix.multilang.metadata.spec.OverlappingRulesException;
import mb.statix.multilang.metadata.spec.SpecFragment;
import mb.statix.multilang.metadata.spec.SpecLoadException;
import mb.statix.multilang.metadata.spec.SpecUtils;
import mb.statix.spec.Rule;
import mb.statix.spec.RuleSet;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mb.nabl2.terms.build.TermBuild.B;

@MultiLangScope
public class SmlBuildSpec implements TaskDef<SmlBuildSpec.Input, Result<Spec, SpecLoadException>> {

    private static final String MODULE_SEPARATOR = "!";

    public static class Input implements Serializable {
        // This input directly specifies the collection of languages to build a specification for
        // Not that we could also have chosen to use Supplier<HashSet<LanguageId>> as key. In that case the
        // task identity would probably be (projectPath, contextId).
        // We choose this identity deliberately for performance (storage usage reasons), because most typical
        // use cases will involve small sets of language. These sets will often be equal in other contexts and projects
        // Therefore this choice of key will have the highest task sharing, and therefore the lowest memory footprint,
        // reinstantiation count and reruns.
        private final HashSet<LanguageId> languages;

        public Input(Collection<LanguageId> languages) {
            this.languages = new HashSet<>(languages);
        }

        public Input(LanguageId language) {
            this.languages = new HashSet<>(Collections.singleton(language));
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return languages.equals(input.languages);
        }

        @Override public int hashCode() {
            return Objects.hash(languages);
        }

        @Override public String toString() {
            return "Input{" +
                "languages=" + languages +
                '}';
        }
    }

    private final Lazy<SpecManager> specManager;
    private final SmlLoadFragment loadFragment;

    @Inject public SmlBuildSpec(
        @MultiLang Lazy<SpecManager> specManager,
        @MultiLang SmlLoadFragment loadFragment
    ) {
        this.specManager = specManager;
        this.loadFragment = loadFragment;
    }

    @Override public String getId() {
        return SmlBuildSpec.class.getCanonicalName();
    }

    @Override public Result<Spec, SpecLoadException> exec(ExecContext context, Input input) {
        return getRequiredFragments(input.languages.toArray(new LanguageId[0]))
            .flatMap(fragmentIds -> fragmentIds.stream()
                // Create SpecFragments from configs
                .map(id -> context.require(loadFragment.createTask(id)))
                .collect(ResultCollector.getWithBaseException(new SpecLoadException("Exception loading fragments")))
                // Sanity check correctness of fragment combination
                .flatMap(fragments -> this.validateIntegrity(fragments, fragmentIds))
                // Load Spec from Fragments.
                .flatMap(fragments -> fragments.stream()
                    .map(this::toSpecResult)
                    .collect(ResultCollector.getWithBaseException(new SpecLoadException("Exception loading fragment specs")))
                    .flatMap(this::validateNoOverlap))
                // Combine all fragments
                .flatMap(specs -> specs.stream()
                    .reduce(SpecUtils::mergeSpecs)
                    // Method reference handles type erasure incorrectly here, hence the lambda
                    .map(x -> Result.<Spec, SpecLoadException>ofOk(x))
                    // When check holds, this orElse call will never be executed
                    .orElse(Result.ofErr(new SpecLoadException("Bug: Tried to build spec from 0 fragments"))))
                // Sanity check for overlapping rules
                .flatMap(this::validateNoOverlappingRules));
    }

    // Transitive fragment dependency calculation

    /**
     * Calculates the set of fragments that must be combined to create a valid spec for all input languages.
     *
     * @param languageIds The languages that should be included in the spec
     * @return Identifiers of fragments that should be loaded. When the result is an error, that would probably
     * indicate a bug.
     */
    private Result<Set<SpecFragmentId>, SpecLoadException> getRequiredFragments(LanguageId... languageIds) {
        return Stream.of(languageIds)
            .map(LanguageId::getId)
            .map(SpecFragmentId::new)
            .map(this::getRequiredFragments)
            .collect(ResultCollector.getWithBaseException(new SpecLoadException("BUG: Error computing spec dependencies for " + Arrays.toString(languageIds))))
            .map(sets -> sets.stream().flatMap(Set::stream).collect(Collectors.toCollection(HashSet::new)));
    }

    /**
     * Transitively calculates the dependencies of the fragment identifier parameter.
     *
     * @param specFragmentId The fragment identifier to calculate the dependencies from
     * @return Identifiers of fragments that should be loaded. When the result is an error, that would probably
     * indicate a bug.
     */
    private Result<Set<SpecFragmentId>, SpecLoadException> getRequiredFragments(SpecFragmentId specFragmentId) {
        return specManager.get().getSpecConfig(specFragmentId).flatMap(config -> config.dependencies().stream()
            .map(this::getRequiredFragments)
            .collect(ResultCollector.getWithBaseException(new SpecLoadException("BUG: Error computing spec dependencies for " + specFragmentId)))
            .map(sets -> {
                final HashSet<SpecFragmentId> result = new HashSet<>(Collections.singleton(specFragmentId));
                sets.forEach(result::addAll);
                return result;
            }));
    }

    // Load fragments

    /**
     * Loads the {@link Spec} of a single fragment. This methods checks whether the loaded fragment does not contain rules
     * for a constraint in another fragment. For example, when the spec contains a rule {@code "base!rule(...)"}, then
     * the module {@code "base"} must be in this fragment.
     *
     * @param specFragment The identifier of the fragment to load.
     * @return The {@link Spec} of the fragment. Error when spec is invalid for composition, or when an IO error occured.
     */
    private Result<Spec, SpecLoadException> toSpecResult(SpecFragment specFragment) {
        final Set<String> moduleNames = specFragment.providedModuleNames();
        return specFragment.toSpecResult().flatMap(spec -> {
            // Collect all rule names for a constraint that is not in this fragment.
            Set<String> remoteConstraintExtensions = spec.rules().getRuleNames().stream()
                .filter(ruleName -> !moduleNames.contains(ruleName.split(MODULE_SEPARATOR)[0]))
                .collect(Collectors.toSet());

            if(!remoteConstraintExtensions.isEmpty()) {
                StringBuilder messageBuilder = new StringBuilder("Rules for predicates from other fragment found:");
                remoteConstraintExtensions.forEach(name -> messageBuilder.append("%n- ").append(name));
                return Result.ofErr(new SpecLoadException(messageBuilder.toString()));
            }
            return Result.ofOk(spec);
        });
    }

    // Integrity validation

    /**
     * Validates that all imports that cross fragment boundaries are satisfied when loading this set of fragments.
     *
     * @param specFragments The fragments to load.
     * @param ids Set of identifiers that belongs to the {@code specFragments}. Used to create good error messages.
     * @return {@code specFragments} when it is valid. Error otherwise.
     */
    private Result<Set<SpecFragment>, SpecLoadException> validateIntegrity(Set<SpecFragment> specFragments, Set<SpecFragmentId> ids) {
        List<String> providedModules  = specFragments.stream()
            .map(SpecFragment::providedModuleNames)
            .flatMap(Set::stream)
            .collect(Collectors.toList());

        Set<String> delayedModules  = specFragments.stream()
            .map(SpecFragment::delayedModuleNames)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        // Check all delayed modules found
        Set<String> unresolvedModules = delayedModules.stream()
            .filter(module -> !providedModules.contains(module))
            .collect(Collectors.toSet());

        if(unresolvedModules.isEmpty()) {
            return Result.ofOk(specFragments);
        }

        String errorMessage = String.format("Specs from %s cannot be combined" +
            "%n- The following imported modules are not resolved: %s" +
            "%n Did you forget to declare a dependency on a fragment?", ids, unresolvedModules);
        return Result.ofErr(new SpecLoadException(errorMessage));
    }

    /**
     * This method validates that the specs do not accidentally have overlapping declarations.
     *
     * @param specs The {@link Spec}s that are to be combined.
     * @return {@code specs} when there are no overlapping definitions. Error otherwise.
     */
    private Result<Set<Spec>, SpecLoadException> validateNoOverlap(Set<Spec> specs) {
        ArrayList<Spec> specList = new ArrayList<>(specs);
        // Check for duplicate rule names.
        // Note that this is not the same as the check in this::toSpecResult
        // The check there checks that a fragment does not contain a rule for another fragment, which is
        // equivalent to extending an interface predicate
        // The check here checks that two fragments do not accidentally
        // locally (not via an interface) declare a rule with the same FQN.
        final HashSet<String> overlappingRuleNames = new HashSet<>();
        for(int i = 0; i < specList.size(); i++) {
            Set<String> ruleNames1 = specList.get(i).rules().getRuleNames();
            for(int j = i + 1; j < specList.size(); j++) {
                Set<String> ruleNames2 = specList.get(j).rules().getRuleNames();
                overlappingRuleNames.addAll(Sets.intersection(ruleNames1, ruleNames2));
            }
        }

        final HashSet<ITerm> overlappingLabels = new HashSet<>();
        for(int i = 0; i < specList.size(); i++) {
            Set<ITerm> labels1 = specList.get(i).allLabels();
            for(int j = i + 1; j < specList.size(); j++) {
                Set<ITerm> labels2 = specList.get(j).allLabels();
                overlappingLabels.addAll(Sets.intersection(labels1, labels2));
            }
        }
        // Auto-generated label. Does not indicate overlap.
        overlappingLabels.remove(B.newAppl("Decl"));

        if(overlappingRuleNames.isEmpty() && overlappingLabels.isEmpty()) {
            return Result.ofOk(specs);
        }

        StringBuilder messageBuilder = new StringBuilder("Overlapping definitions in combined specification");

        if(!overlappingRuleNames.isEmpty()) {
            messageBuilder.append(String.format("%n- The constraints %s define rules in multiple fragments", overlappingRuleNames));
        }
        if(!overlappingLabels.isEmpty()) {
            messageBuilder.append(String.format("%n- The labels %s are defined in multiple fragments", overlappingLabels));
        }

        return Result.ofErr(new SpecLoadException(messageBuilder.toString()));
    }

    /**
     * Applies {@link RuleSet#getAllEquivalentRules()} on the rules of {@code combinedSpec} and returns an error when
     * overlap is reported. This should not occur when {@link this#validateNoOverlap(Set)} and
     * {@link this#toSpecResult(SpecFragment)} pass already. When it does, it probably points to a bug there.
     *
     * @param combinedSpec Result of combining specs of different fragments
     * @return {@code combinedSpec} when there are no overlapping rules, error otherwise.
     */
    private Result<Spec, SpecLoadException> validateNoOverlappingRules(Spec combinedSpec) {
        final ListMultimap<String, Rule> rulesWithEquivalentPatterns = combinedSpec.rules().getAllEquivalentRules();
        if(!rulesWithEquivalentPatterns.isEmpty()) {
            return Result.ofErr(new OverlappingRulesException(rulesWithEquivalentPatterns));
        }

        return Result.ofOk(combinedSpec);
    }
}
