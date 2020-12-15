package mb.statix.multilang.metadata;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import mb.common.result.Result;
import mb.common.result.ResultCollector;
import mb.nabl2.terms.ITerm;
import mb.pie.api.Pie;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.metadata.spec.OverlappingRulesException;
import mb.statix.multilang.metadata.spec.SpecConfig;
import mb.statix.multilang.metadata.spec.SpecFragment;
import mb.statix.multilang.metadata.spec.SpecLoadException;
import mb.statix.multilang.metadata.spec.SpecUtils;
import mb.statix.spec.Rule;
import mb.statix.spec.RuleSet;
import mb.statix.spec.Spec;
import org.immutables.value.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service that manages all the statically loaded data used for multilanguage analysis.
 * Note that dynamic configuration (as supplied by {@link mb.statix.multilang.pie.config.SmlReadConfigYaml the
 * yaml config}) can override these values.
 */
@Value.Immutable
public abstract class AnalysisContextService implements LanguageMetadataManager, ContextPieManager, SpecManager {

    private static final String MODULE_SEPARATOR = "!";

    @Value.Parameter public abstract Map<LanguageId, ContextId> defaultLanguageContexts();

    @Value.Parameter public abstract Map<LanguageId, Supplier<LanguageMetadata>> languageMetadataSuppliers();

    @Value.Parameter public abstract Map<SpecFragmentId, SpecConfig> specConfigs();

    @Value.Parameter public abstract Pie platformPie();

    // Map used to cache language metadata instances, so that they will not be recomputed by subsequent accesses.
    private final ConcurrentHashMap<LanguageId, LanguageMetadata> languageMetadataCache = new ConcurrentHashMap<>();

    @Override
    public Result<LanguageMetadata, MultiLangAnalysisException> getLanguageMetadataResult(LanguageId languageId) {
        if(!languageMetadataSuppliers().containsKey(languageId)) {
            return Result.ofErr(new MultiLangAnalysisException("No language metadata for id " + languageId));
        }

        // Consult cache to return value, else compute from supplier
        return Result.ofOk(languageMetadataCache.computeIfAbsent(languageId, lid -> languageMetadataSuppliers().get(lid).get()));
    }

    @Override public Set<LanguageId> getContextLanguages(ContextId contextId) {
        return defaultLanguageContexts()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(contextId))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    @Override public ContextId getDefaultContextId(LanguageId languageId) {
        return defaultLanguageContexts().getOrDefault(languageId, new ContextId(languageId.getId()));
    }

    @Override public Pie buildPieForContext() throws MultiLangAnalysisException {
        Pie[] languagePies = languageMetadataSuppliers().keySet().stream()
            .map(this::getLanguageMetadataResult)
            .collect(ResultCollector.getWithBaseException(new MultiLangAnalysisException("Exception fetching language metadata")))
            .unwrap()
            .stream()
            .map(LanguageMetadata::languagePie)
            .toArray(Pie[]::new);

        return platformPie()
            .createChildBuilder(languagePies)
            .build();
    }

    public static ImmutableAnalysisContextService.Builder builder() {
        return ImmutableAnalysisContextService.builder();
    }

    @Override public Result<Spec, SpecLoadException> getSpecResult(LanguageId... languageIds) {
        Result<Set<SpecFragmentId>, SpecLoadException> fragmentIds = getRequiredFragments(languageIds);
        return fragmentIds.flatMap(ids -> ids.stream().map(specConfigs()::get)
            .map(SpecConfig::load)
            .collect(ResultCollector.getWithBaseException(new SpecLoadException("Error loading base exception")))
            .flatMap(fragments -> this.validateIntegrity(fragments, ids))
            .flatMap(this::toSpec));
    }

    private Result<Spec, SpecLoadException> toSpec(Set<SpecFragment> specFragments) {
        return specFragments.stream()
            .map(this::toSpecResult)
            .collect(ResultCollector.getWithBaseException(new SpecLoadException("Error loading spec fragments")))
            .flatMap(this::validateNoOverlap)
            .flatMap(specs -> specs.stream()
                .reduce(SpecUtils::mergeSpecs)
                // Method reference handles type erasure incorrectly here, hence the lambda
                .map(x -> Result.<Spec, SpecLoadException>ofOk(x))
                // When check holds, this orElse call will never be executed
                .orElse(Result.ofErr(new SpecLoadException("Bug: Tried to build spec from 0 fragments"))))
            .flatMap(this::validateNoOverlappingRules);
    }

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
            "%n- The following imported modules are not resolved: %s", ids, unresolvedModules);
        return Result.ofErr(new SpecLoadException(errorMessage));
    }

    private Result<Set<Spec>, SpecLoadException> validateNoOverlap(Set<Spec> specs) {
        Set<String> overlappingRuleNames = specs.stream()
            .map(Spec::rules)
            .<Set<String>>map(RuleSet::getRuleNames)
            .reduce(Collections.emptySet(), Sets::intersection);

        Set<ITerm> overlappingLabels = specs.stream()
            .map(Spec::allLabels)
            .reduce(Collections.emptySet(), Sets::intersection);

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

    private Result<Spec, SpecLoadException> validateNoOverlappingRules(Spec combinedSpec) {
        final ListMultimap<String, Rule> rulesWithEquivalentPatterns = combinedSpec.rules().getAllEquivalentRules();
        if(!rulesWithEquivalentPatterns.isEmpty()) {
            return Result.ofErr(new OverlappingRulesException(rulesWithEquivalentPatterns));
        }

        return Result.ofOk(combinedSpec);
    }

    private Result<Set<SpecFragmentId>, SpecLoadException> getRequiredFragments(LanguageId... languageIds) {
        return Stream.of(languageIds)
            .map(LanguageId::getId)
            .map(SpecFragmentId::new)
            .map(this::getRequiredFragments)
            .collect(ResultCollector.getWithBaseException(new SpecLoadException("Error computing spec dependencies for " + Arrays.toString(languageIds))))
            .map(sets -> sets.stream().flatMap(Set::stream).collect(Collectors.toCollection(HashSet::new)));
    }

    private Result<Set<SpecFragmentId>, SpecLoadException> getRequiredFragments(SpecFragmentId specFragmentId) {
        if(specConfigs().containsKey(specFragmentId)) {
            final SpecConfig config = specConfigs().get(specFragmentId);
            return config.dependencies().stream()
                .map(this::getRequiredFragments)
                .collect(ResultCollector.getWithBaseException(new SpecLoadException("Error computing spec dependencies for " + specFragmentId)))
                .map(sets -> {
                    final HashSet<SpecFragmentId> result = new HashSet<>(Collections.singleton(specFragmentId));
                    sets.forEach(result::addAll);
                    return result;
                });
        }

        // When check method holds, this cannot occur.
        return Result.ofErr(new SpecLoadException("No spec config for " + specFragmentId));
    }

    @Value.Check public void checkSpecDependenciesSatisfiable() {
        // Check if each language has spec configuration
        languageMetadataSuppliers().keySet().forEach(lid -> {
            if(!specConfigs().containsKey(new SpecFragmentId(lid.getId()))) {
                throw new IllegalStateException("No spec config for language " + lid);
            }
        });

        // Check if all fragment dependencies are provided
        specConfigs().forEach((specId, conf) -> conf.dependencies().forEach(depId -> {
            if(!specConfigs().containsKey(depId)) {
                throw new IllegalStateException(String.format("Spec %1$s has a dependency on %2$s, but no config for %2$s is found.", specId, depId ));
            }
        }));
    }
}
