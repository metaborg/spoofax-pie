package mb.constraint.common.stratego;

import mb.resource.ResourceService;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class ConstraintPrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    public ConstraintPrimitiveLibrary(ResourceService resourceService) {
        add(new C_get_resource_analysis(resourceService));
        add(new C_get_resource_partial_analysis(resourceService));
    }

    @Override public String getOperatorRegistryName() {
        return "ConstraintPrimitiveLibrary";
    }
}
