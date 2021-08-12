package mb.stratego.common;

import mb.stratego.common.primitive.StrategoVersionPrimitive;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class StrategoPrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public StrategoPrimitiveLibrary() {
        add(new StrategoVersionPrimitive());
    }

    @Override public String getOperatorRegistryName() {
        return "Stratego";
    }
}
