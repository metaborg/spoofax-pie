package mb.spoofax2.common.primitive;

import mb.resource.ResourceKeyString;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.util.SeparatorUtil;
import mb.spoofax2.common.primitive.generic.ASpoofaxPrimitive;
import mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

public class RelativeSourceOrIncludePath extends ASpoofaxPrimitive {
    private final ResourceService resourceService;

    public RelativeSourceOrIncludePath(ResourceService resourceService) {
        super("language_relative_source_or_include_path", 0, 1);
        this.resourceService = resourceService;
    }

    @Override public @Nullable IStrategoTerm call(
        IStrategoTerm current,
        Strategy[] svars,
        IStrategoTerm[] tvars,
        ITermFactory termFactory,
        IContext context
    ) {
        if(!TermUtils.isString(tvars[0])) return null;
        if(!TermUtils.isString(current)) return null;
        final String resourceName = TermUtils.toJavaString(current);
        final String languageId = TermUtils.toJavaString(tvars[0]);

        final Spoofax2ProjectContext projectContext = getSpoofax2ProjectContext(context);
        final ResourcePath path = resourceService.getResourcePath(ResourceKeyString.parse(resourceName));

        ResourcePath base = projectContext.projectPath;
        final Iterable<ResourcePath> sourceLocations = projectContext.sourceAndIncludePaths(languageId);
        for(ResourcePath sourceLocation : sourceLocations) {
            if(path.startsWith(sourceLocation)) {
                base = sourceLocation;
                break;
            }
        }
        final String relativePath;
        try {
            relativePath = base.relativize(path);
        } catch(ResourceRuntimeException e) {
            return null;
        }
        // Convert to UNIX separators (/) as meta-languages use / in module names and expect paths to also use this.
        return termFactory.makeString(SeparatorUtil.convertCurrentToUnixSeparator(relativePath));
    }
}
