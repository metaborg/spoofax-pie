package mb.statix.multilang;

public class FileAnalysisException extends MultiLangAnalysisException {

    public FileAnalysisException(AnalysisResults.FileKey fileKey, Throwable throwable) {
        super(fileKey.getResource(), fileKey.getResource().toString(), throwable);
    }
}
