package mb.statix.multilang.spec;

public class SpecLoadException extends RuntimeException { // Note: used in @Value.Check method, and therefore needs to extend RuntimeException
    public SpecLoadException(String s) {
        super(s);
    }

    public SpecLoadException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
