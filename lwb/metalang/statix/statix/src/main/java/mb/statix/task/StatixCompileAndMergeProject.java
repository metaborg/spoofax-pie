package mb.statix.task;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Compiles a Statix project and merges the specification files into one.
 */
@StatixScope
public class StatixCompileAndMergeProject implements TaskDef<StatixCompileAndMergeProject.Args, Result<IStrategoTerm, ?>> {

    // TODO: Extract this Args class (and similar ones) into a separate class?
    public static class Args implements Serializable {
        /** The root directory of the project. */
        public final ResourcePath rootDirectory;
        /** The file being completed. */
        public final ResourceKey file;

        /**
         * Initializes a new instance of the {@link StatixCompileAndMergeProject.Args} class.
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
            return equals((StatixCompileAndMergeProject.Args)o);
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
        protected boolean equals(StatixCompileAndMergeProject.Args that) {
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
            return "StatixCompileMergeProject.Args{" +
                "rootDirectory=" + rootDirectory + ", " +
                "file=" + file +
                "}";
        }
    }

    private StatixClassLoaderResources classLoaderResources;
    private final StatixCompileProject compileProject;
    private StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider;

    @Inject public StatixCompileAndMergeProject(
        StatixClassLoaderResources classLoaderResources,
        StatixCompileProject compileProject,
        StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider
    ) {
        this.classLoaderResources = classLoaderResources;
        this.compileProject = compileProject;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<IStrategoTerm, ?> exec(ExecContext context, StatixCompileAndMergeProject.Args input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsLocalResource(StatixEvaluateTest.Args.class), ResourceStampers.hashFile());

        final ResourcePath rootDirectory = input.rootDirectory;
        final ResourceKey file = input.file;
        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();

        context.require(compileProject, input.rootDirectory).mapThrowingOrElse(compileProjectResult -> {
            final List<IStrategoTerm> localSpecs = compileProjectResult.stream().map(p -> p.spec).collect(Collectors.toList());
            strategoRuntime.invoke()
        });

        // TODO: Call Stratego and merge the specs
        // stx--merge-spec-aterms
//        context.require(compileProject, );
        return null;
    }
}
