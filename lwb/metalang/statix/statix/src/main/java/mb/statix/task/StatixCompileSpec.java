package mb.statix.task;

import com.google.common.collect.ListMultimap;
import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
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
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Compiles a Statix project and returns the Statix spec.
 */
@StatixScope
public class StatixCompileSpec implements TaskDef<StatixCompileSpec.Args, Result<Spec, ?>> {

    // TODO: Extract this Args class (and similar ones) into a separate class?
    public static class Args implements Serializable {
        /** The root directory of the project. */
        public final ResourcePath rootDirectory;
        /** The file being completed. */
        public final ResourceKey file;

        /**
         * Initializes a new instance of the {@link StatixCompileSpec.Args} class.
         * @param rootDirectory the root directory of the project
         * @param file the file being completed
         */
        public Args(ResourcePath rootDirectory, ResourceKey file) {
            this.rootDirectory = rootDirectory;
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            return equals((StatixCompileSpec.Args)o);
        }

        /**
         * Determines whether this object is equal to the specified object.
         *
         * Note: this method does not check whether the type of the argument is exactly the same.
         *
         * @param that the object to compare to
         * @return {@code true} when this object is equal to the specified object;
         * otherwise, {@code false}
         */
        protected boolean equals(StatixCompileSpec.Args that) {
            if (this == that) return true;
            return this.rootDirectory.equals(that.rootDirectory)
                && this.file.equals(that.file);
        }

        @Override public int hashCode() {
            return Objects.hash(
                this.rootDirectory,
                this.file
            );
        }

        @Override public String toString() {
            return "StatixCompileSpec.Args{" +
                "rootDirectory=" + rootDirectory + ", " +
                "file=" + file +
                "}";
        }
    }

    private StatixClassLoaderResources classLoaderResources;
    private StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider;
    private StatixCompileAndMergeProject compileMergedProject;

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
    public Result<Spec, ?> exec(ExecContext context, StatixCompileSpec.Args input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsLocalResource(StatixEvaluateTest.Args.class), ResourceStampers.hashFile());

        final ResourcePath rootDirectory = input.rootDirectory;
        final ResourceKey file = input.file;
        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();
        return context.require(compileMergedProject, new StatixCompileAndMergeProject.Args(rootDirectory, file)).mapThrowing(specAst -> {
            @Nullable final Spec spec = toSpec(specAst, strategoRuntime.getTermFactory());
            @Nullable final String overlappingRulesMsg = checkNoOverlappingRules(spec);
            if(overlappingRulesMsg != null) {
                // Invalid specification
                return Result.ofErr(new IllegalStateException(overlappingRulesMsg));
            }
            return Result.ofOk(spec);
        });
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

    /**
     * Reports any overlapping rules in the specification.
     *
     * @param spec the specification to check
     * @return a String message when the specification has no overlapping rules;
     * otherwise, {@code null}.
     */
    private static @Nullable String checkNoOverlappingRules(Spec spec) {
        final ListMultimap<String, Rule> rulesWithEquivalentPatterns = spec.rules().getAllEquivalentRules();
        if(!rulesWithEquivalentPatterns.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Found rules with equivalent patterns.\n");
            for(Map.Entry<String, Collection<Rule>> entry : rulesWithEquivalentPatterns.asMap().entrySet()) {
                sb.append("Overlapping rules for: ").append(entry.getKey()).append("\n");
                for(Rule rule : entry.getValue()) {
                    sb.append("* ").append(rule).append("\n");
                }
            }
            return sb.toString();
        }
        return null;
    }
}
