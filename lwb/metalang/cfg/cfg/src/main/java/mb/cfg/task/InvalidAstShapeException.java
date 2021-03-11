package mb.cfg.task;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class InvalidAstShapeException extends RuntimeException {
    public final String expected;
    public final IStrategoTerm ast;

    public InvalidAstShapeException(String expected, IStrategoTerm ast) {
        super();
        this.expected = expected;
        this.ast = ast;
    }

    @Override public String getMessage() {
        return "BUG: invalid AST shape, expected " + expected + " in AST: " + ast;
    }
}
