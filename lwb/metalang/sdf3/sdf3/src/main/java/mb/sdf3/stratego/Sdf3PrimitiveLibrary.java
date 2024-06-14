package mb.sdf3.stratego;

import mb.stratego.common.primitive.FailingPrimitive;

import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class Sdf3PrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public Sdf3PrimitiveLibrary() {
        add(new Sdf3PpLanguageSpecNamePrimitive());
        add(new Sdf3PlaceholderCharsPrimitive());
        add(new Sdf3SpoofaxVersionPrimitive());
    }

    @Override public String getOperatorRegistryName() {
        return "Sdf3PrimitiveLibrary";
    }
}
