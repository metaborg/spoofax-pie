package mb.statix.multilang;

public class ProjectAnalysisException extends MultiLangAnalysisException {

    public ProjectAnalysisException(LanguageId language, Throwable throwable) {
        super(language.getId(), throwable);
    }
}
