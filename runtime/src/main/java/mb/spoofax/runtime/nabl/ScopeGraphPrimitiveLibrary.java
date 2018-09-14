package mb.spoofax.runtime.nabl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import mb.spoofax.runtime.stratego.primitive.GenericPrimitiveLibrary;
import org.spoofax.interpreter.library.AbstractPrimitive;

import java.util.Set;

public class ScopeGraphPrimitiveLibrary extends GenericPrimitiveLibrary {
    public static final String name = "ScopeGraphPrimitiveLibrary";

    @Inject public ScopeGraphPrimitiveLibrary(@Named(name) Set<AbstractPrimitive> primitives) {
        super(primitives, ScopeGraphPrimitiveLibrary.name);
    }
}
