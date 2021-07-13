package mb.sdf3_ext_statix.stratego;

import mb.stratego.common.primitive.FailingPrimitive;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class Sdf3ExtStatixPrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public Sdf3ExtStatixPrimitiveLibrary() {
        add(new Sdf3PpLanguageSpecNamePrimitive());
    }

    @Override public String getOperatorRegistryName() {
        return "Sdf3ExtStatixPrimitiveLibrary";
    }
}
