package mb.statix.multilang.pie;

import com.google.common.collect.ListMultimap;
import dagger.Lazy;
import mb.common.result.Result;
import mb.common.result.ResultCollector;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.LanguageMetadataManager;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.MultiLangScope;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.multilang.spec.SpecLoadException;
import mb.statix.spec.Rule;
import mb.statix.spec.Spec;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

@MultiLangScope
public class SmlBuildSpec implements TaskDef<SmlBuildSpec.Input, Result<Spec, SpecLoadException>> {
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

        @Override public boolean equals(Object o) {
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

    private final Lazy<LanguageMetadataManager> languageMetadataManager;
    private final Logger logger;

    @Inject public SmlBuildSpec(@MultiLang Lazy<LanguageMetadataManager> languageMetadataManager, LoggerFactory loggerFactory) {
        this.languageMetadataManager = languageMetadataManager;
        logger = loggerFactory.create(SmlBuildSpec.class);
    }

    @Override public String getId() {
        return SmlBuildSpec.class.getCanonicalName();
    }

    @Override public Result<Spec, SpecLoadException> exec(ExecContext context, Input input) {
        return input.languages.stream()
            .map(lid -> languageMetadataManager.get().getLanguageMetadataResult(lid))
            .collect(ResultCollector.getWithBaseException(new SpecLoadException("Exception getting language metadata", false)))
            .flatMap(languageMetadataSet -> languageMetadataSet.stream()
                .map(LanguageMetadata::statixSpec)
                .reduce(SpecBuilder::merge)
                .map(SpecBuilder::toSpecResult)
                .map(spec -> spec.ifOk(this::validateOverlappingRules))
                .orElse(Result.ofErr(new SpecLoadException("Doing analysis without specs is not allowed"))));
    }

    private void validateOverlappingRules(Spec combinedSpec) {
        final ListMultimap<String, Rule> rulesWithEquivalentPatterns = combinedSpec.rules().getAllEquivalentRules();
        if(!rulesWithEquivalentPatterns.isEmpty()) {
            logger.error("+--------------------------------------+");
            logger.error("| FOUND RULES WITH EQUIVALENT PATTERNS |");
            logger.error("+--------------------------------------+");
            for(Map.Entry<String, Collection<Rule>> entry : rulesWithEquivalentPatterns.asMap().entrySet()) {
                logger.error("| Overlapping rules for: {}", entry.getKey());
                for(Rule rule : entry.getValue()) {
                    logger.error("| * {}", rule);
                }
            }
            logger.error("+--------------------------------------+");
        }
    }
}
