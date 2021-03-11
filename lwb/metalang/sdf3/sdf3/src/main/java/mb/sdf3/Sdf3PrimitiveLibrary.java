package mb.sdf3;

import mb.stratego.common.primitive.FailingPrimitive;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class Sdf3PrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public Sdf3PrimitiveLibrary() {
        add(new Sdf3PpLanguageSpecNamePrimitive());
        add(new FailingPrimitive("SSL_EXT_placeholder_chars"));
    }

    @Override public String getOperatorRegistryName() {
        return "Sdf3PrimitiveLibrary";
    }
}
