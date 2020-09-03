package mb.statix.multilang.metadata.spec;

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

    public SpecLoadException(String s, boolean includeMessage) {
        super(s, includeMessage);
    }
}
