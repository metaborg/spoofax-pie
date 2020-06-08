package mb.statix.multilang;

import mb.common.message.KeyedMessages;
import mb.common.util.CollectionView;
import mb.pie.api.Task;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.spoofax.terms.TermFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AnalysisContext implements Serializable {

    private final Map<LanguageId, LanguageMetadata> languages = new HashMap<>();
    private CachedAnalysis cachedAnalysis = CachedAnalysis.builder().build();
    private @Nullable Level logLevel;

    public void register(LanguageMetadata language) {
        if (!languages.containsKey(language.languageId())) {
            languages.put(language.languageId(), language);
            cachedAnalysis = CachedAnalysis.builder().build(); // Added spec invalidates scope graph
        }
    }

    public Task<@Nullable KeyedMessages> createAnalyzerTask() {
        return new StatixAnalysisTaskDef(new TermFactory()).createTask(new StatixAnalysisTaskDef.Input(this));
    }

    public void setLogLevel(@Nullable Level logLevel) {
        this.logLevel = logLevel;
    }

    public CollectionView<LanguageMetadata> languages() {
        return CollectionView.copyOf(languages.values());
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public CachedAnalysis getCachedAnalysis() {
        return cachedAnalysis;
    }

    public void updateCachedAnalysis(CachedAnalysis newAnalysis) {
        this.cachedAnalysis = newAnalysis;
    }

    public void clear() {
        languages.clear();
        cachedAnalysis = CachedAnalysis.builder().build();
        logLevel = null;
    }

    // TODO: Good hashing/equals for incrementality
}
