package mb.constraint.common.stratego;

import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.List;
import java.util.Optional;

public class C_get_resource_partial_analysis extends ConstraintContextPrimitive {
    private final ResourceService resourceService;

    public C_get_resource_partial_analysis(ResourceService resourceService) {
        super(C_get_resource_partial_analysis.class.getSimpleName());
        this.resourceService = resourceService;
    }

    @Override protected Optional<? extends IStrategoTerm> call(
        ConstraintAnalyzerContext context,
        IStrategoTerm sterm,
        List<IStrategoTerm> sterms,
        ITermFactory factory
    ) throws InterpreterException {
        if(!Tools.isTermString(sterm)) {
            throw new InterpreterException("Expect a resource path");
        }
        final String resourceStr = Tools.asJavaString(sterm);
        final ResourceKey resource = resourceService.getResourceKey(ResourceKeyString.parse(resourceStr));
        final ConstraintAnalyzer.@Nullable Result result = context.getResult(resource);
        final @Nullable IStrategoTerm analysis;
        if(result != null) {
            analysis = result.analysis;
        } else {
            analysis = null;
        }
        return Optional.ofNullable(analysis);
    }
}
