package mb.statix.multilang;

public class ConfigurationException extends MultiLangAnalysisException {

    public ConfigurationException(String s) {
        super(s);
    }

    public ConfigurationException(Throwable throwable) {
        super(throwable, false);
    }

    public ConfigurationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
