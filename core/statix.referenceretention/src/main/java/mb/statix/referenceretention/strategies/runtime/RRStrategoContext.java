package mb.statix.referenceretention.strategies.runtime;

import mb.tego.strategies.runtime.TegoRuntime;

/**
 * The Stratego context object for reference retention.
 */
public final class RRStrategoContext {

    public final TegoRuntime tegoRuntime;

    public RRStrategoContext(TegoRuntime tegoRuntime) {
        this.tegoRuntime = tegoRuntime;
    }
}
