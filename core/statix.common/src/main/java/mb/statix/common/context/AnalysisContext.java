package mb.statix.common.context;

import io.usethesource.capsule.Set;
import mb.common.message.KeyedMessages;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.ImmutableTermVar;
import mb.nabl2.terms.unification.OccursException;
import mb.nabl2.terms.unification.u.IUnifier;
import mb.nabl2.terms.unification.u.PersistentUnifier;
import mb.nabl2.terms.unification.ud.PersistentUniDisunifier;
import mb.pie.api.Task;
import mb.statix.scopegraph.IScopeGraph;
import mb.statix.scopegraph.reference.ScopeGraph;
import mb.statix.scopegraph.terms.Scope;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;
import org.spoofax.terms.TermFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AnalysisContext implements Serializable {

    private final Map<String, LanguageMetadata> languages = new HashMap<>();
    private @Nullable State cachedResult;

    private @Nullable Level logLevel;

    public void register(LanguageMetadata language) {
        if (!languages.containsKey(language.languageId())) {
            languages.put(language.languageId(), language);
            cachedResult = null; // Added spec invalidate scope graph
        } // Else update/validate equality?
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

    public @Nullable State getCachedResult() {
        return cachedResult;
    }

    public void updateSolverResult(State newResult) {
        this.cachedResult = newResult;
    }

    public void clear() {
        languages.clear();
        cachedResult = null;
        logLevel = null;
    }

    // TODO: Good hashing/equals for incrementality
}
