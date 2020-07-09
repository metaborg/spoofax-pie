package mb.statix.multilang.spec;

import mb.statix.multilang.MultiLangAnalysisException;

public class SpecLoadException extends MultiLangAnalysisException {
    public SpecLoadException(String s) {
        super(s);
    }

    public SpecLoadException(Throwable throwable) {
        super(throwable);
    }

    public SpecLoadException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
