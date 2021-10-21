package mb.statix.task;

import com.google.common.collect.ListMultimap;
import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
import mb.statix.spec.Rule;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Compiles a Statix project and returns the Statix spec.
 */
@StatixScope
public class StatixCompileSpec implements TaskDef<ResourcePath, Result<Spec, ?>> {

    private final StatixClassLoaderResources classLoaderResources;
    private final StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider;
    private final StatixCompileAndMergeProject compileMergedProject;

    @Inject public StatixCompileSpec(
        StatixClassLoaderResources classLoaderResources,
        StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider,
        StatixCompileAndMergeProject compileMergedProject
    ) {
        this.classLoaderResources = classLoaderResources;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
        this.compileMergedProject = compileMergedProject;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Spec, ?> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());

        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();
        return context.require(compileMergedProject, rootDirectory).mapThrowing(specAst -> toSpec(specAst, strategoRuntime.getTermFactory()));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    /**
     * Gets the Statix specification from the specified term.
     *
     * @param specAst the specification term
     * @param termFactory the term factory
     * @return the specification
     */
    public static Spec toSpec(IStrategoTerm specAst, ITermFactory termFactory) throws InterpreterException {
        final ITerm specTerm = new StrategoTerms(termFactory).fromStratego(specAst);
        return StatixTerms.spec().match(specTerm).orElseThrow(() -> new InterpreterException("Expected spec, got " + specTerm));
    }
}
