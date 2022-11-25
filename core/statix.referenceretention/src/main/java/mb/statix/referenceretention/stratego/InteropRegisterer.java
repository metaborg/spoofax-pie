package mb.statix.referenceretention.stratego;

import org.strategoxt.lang.JavaInteropRegisterer;

/**
 * Interop registerer for the reference retention strategies.
 * <p>
 * Stratego strategies that are defined as Stratego primitives are registered using a Primitive Library,
 * but Stratego strategies that are defined as classes are registered using an Interop Registerer.
 */
public final class InteropRegisterer extends JavaInteropRegisterer {
    public InteropRegisterer() {
        super(
            rr_create_placeholder_0_1.instance,
            rr_fix_references_0_0.instance,
            rr_lock_reference_0_1.instance
        );
    }
}

