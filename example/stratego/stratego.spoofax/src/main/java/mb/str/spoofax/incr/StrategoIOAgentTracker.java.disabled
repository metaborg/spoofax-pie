package mb.str.spoofax.incr;

import mb.stratego.build.util.IOAgentTracker;
import mb.stratego.common.StrategoIOAgent;
import org.spoofax.interpreter.library.IOAgent;

public class StrategoIOAgentTracker implements IOAgentTracker {
    private final StrategoIOAgent ioAgent;

    public StrategoIOAgentTracker(StrategoIOAgent ioAgent) {
        this.ioAgent = ioAgent;
    }

    @Override public IOAgent agent() {
        return ioAgent;
    }

    @Override public String stdout() {
        return ""; // TODO: implement
    }

    @Override public String stderr() {
        return ""; // TODO: implement
    }
}
