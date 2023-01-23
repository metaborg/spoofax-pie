package mb.statix.referenceretention.stratego;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

import javax.inject.Inject;

public class RRPrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    @Inject public RRPrimitiveLibrary() {
        add(new RRFixReferencesStrategy());
        add(new RRCreatePlaceholderStrategy());
        add(new RRLockReferenceStrategy());
    }

    @Override public String getOperatorRegistryName() {
        return "RRPrimitiveLibrary";
    }
}
