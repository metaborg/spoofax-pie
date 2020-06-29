package mb.statix.multilang.pie;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.spec.Spec;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

public class SmlBuildSpec implements TaskDef<SmlBuildSpec.Input, Spec> {
    public static class Input implements Serializable {
        private final HashSet<LanguageId> languages;

        public Input(Collection<LanguageId> languages) {
            this.languages = new HashSet<>(languages);
        }
    }

    private final AnalysisContextService analysisContextService;

    @Inject public SmlBuildSpec(AnalysisContextService analysisContextService) {
        this.analysisContextService = analysisContextService;
    }

    @Override public String getId() {
        return SmlBuildSpec.class.getCanonicalName();
    }

    @Override public Spec exec(ExecContext context, Input input) throws Exception {
        return input.languages.stream()
            .map(analysisContextService::getLanguageSpec)
            .reduce(SpecBuilder::merge)
            .orElseThrow(() -> new MultiLangAnalysisException("Doing analysis without specs is not allowed"))
            .toSpec();
    }
}
