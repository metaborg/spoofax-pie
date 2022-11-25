package mb.statix.referenceretention.stratego;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class RRPrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public RRPrimitiveLibrary() {
        add(new RRFixReferencesStrategy());
        add(new RRCreatePlaceholderStrategy());
        add(new RRLockReferenceStrategy());
    }

    @Override public String getOperatorRegistryName() {
        return "RRPrimitiveLibrary";
    }
}
