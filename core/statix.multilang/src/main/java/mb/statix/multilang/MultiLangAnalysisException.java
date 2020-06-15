package mb.statix.multilang;

public class MultiLangAnalysisException extends RuntimeException {

    public MultiLangAnalysisException(String s) {
        super(s);
    }

    public MultiLangAnalysisException(Throwable throwable) {
        super(throwable);
    }

    public MultiLangAnalysisException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
