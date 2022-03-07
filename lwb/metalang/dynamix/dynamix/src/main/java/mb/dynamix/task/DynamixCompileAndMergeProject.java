package mb.dynamix.task;

import mb.common.result.Result;
import mb.dynamix.DynamixClassLoaderResources;
import mb.dynamix.DynamixScope;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.common.StrategoRuntime;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compiles a Dynamix project and merges the specification files into one.
 */
@DynamixScope
public class DynamixCompileAndMergeProject implements TaskDef<ResourcePath, Result<IStrategoTerm, ?>> {
    private final DynamixClassLoaderResources classLoaderResources;
    private final DynamixCompileProject compileProject;
    private final DynamixGetStrategoRuntimeProvider getStrategoRuntimeProvider;

    @Inject public DynamixCompileAndMergeProject(
        DynamixClassLoaderResources classLoaderResources,
        DynamixCompileProject compileProject,
        DynamixGetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        this.classLoaderResources = classLoaderResources;
        this.compileProject = compileProject;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override
    public Result<IStrategoTerm, ?> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());

        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();
        final ITermFactory termFactory = strategoRuntime.getTermFactory();

        return context.require(compileProject, rootDirectory).mapThrowing(compileProjectResult -> {
            final List<IStrategoTerm> localSpecs = compileProjectResult.stream().map(p -> p.spec).collect(Collectors.toList());
            final IStrategoList inputList = termFactory.makeList(localSpecs);
            return strategoRuntime.invoke("dx--merge-specifications", inputList);
        });
    }
}
