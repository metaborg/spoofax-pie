package mb.spt.task;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.spt.SptClassLoaderResources;
import mb.spoofax.core.language.testrunner.TestResults;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

public class SptShowTestSuitesResults implements TaskDef<SptShowTestSuitesResults.Args, CommandFeedback> {

    private final SptClassLoaderResources classLoaderResources;
    private final SptRunTestSuites checkForOutputAggregator;

    public static class Args implements Serializable {
        public final ResourcePath rootDir;
        public final ResourcePath directory;

        public Args(ResourcePath rootDir, ResourcePath directory) {
            this.rootDir = rootDir;
            this.directory = directory;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return directory.equals(args.directory) && rootDir.equals(args.rootDir);
        }

        @Override
        public int hashCode() {
            return Objects.hash(directory, rootDir);
        }

        @Override
        public String toString() {
            return "SptShowTestSuitesResults$Args{" + "directory=" + directory + ", rootDir=" + rootDir + '}';
        }
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public CommandFeedback exec(ExecContext context, Args input) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        TestResults result = context.require(checkForOutputAggregator, new SptRunTestSuites.Input(input.directory, input.rootDir));
        return CommandFeedback.of(ShowFeedback.showTestResults(result));
    }

    @Inject
    public SptShowTestSuitesResults(SptClassLoaderResources classLoaderResources, SptRunTestSuites checkForOutputAggregator) {
        this.classLoaderResources = classLoaderResources;
        this.checkForOutputAggregator = checkForOutputAggregator;
    }
}
