package mb.spoofax.runtime.stratego.primitive;

import java.util.Set;

import org.spoofax.interpreter.library.AbstractPrimitive;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ScopeGraphPrimitiveLibrary extends GenericPrimitiveLibrary {
    public static final String name = "ScopeGraphPrimitiveLibrary";

    @Inject public ScopeGraphPrimitiveLibrary(@Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, ScopeGraphPrimitiveLibrary.name);
    }
}
