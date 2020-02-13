package mb.spoofax2.common.primitive;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax2.common.primitive.generic.ASpoofaxContextPrimitive;
import mb.spoofax2.common.primitive.generic.Spoofax2Context;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class ProjectSrcGenDirectory extends ASpoofaxContextPrimitive {
    private final Logger log;
    private final ResourceService resourceService;

    public ProjectSrcGenDirectory(LoggerFactory loggerFactory, ResourceService resourceService) {
        super("project_srcgen_dir", 0, 0);
        this.log = loggerFactory.create(ProjectSrcGenDirectory.class);
        this.resourceService = resourceService;
    }

    @Override protected IStrategoTerm call(
        IStrategoTerm current,
        Strategy[] svars,
        IStrategoTerm[] tvars,
        ITermFactory termFactory,
        IContext strategoContext,
        Spoofax2Context context
    ) throws InterpreterException {
        log.warn("Attempting to get project src-gen directory, but project resources are not yet supported in Spoofax 3, trying to get language src-gen directory instead");
        final ResourcePath srcGenPath = context.languagePath.appendSegment("src-gen/statix/"); // HACK: return src-gen/statix/ to make Statix work for now.
        return termFactory.makeString(resourceService.toStringRepresentation(srcGenPath));
    }
}
