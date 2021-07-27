package mb.spt.task;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.spt.SptClassLoaderResources;
import mb.spoofax.core.language.model.MultiTestSuiteRun;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

public class SptRunTestSuites implements TaskDef<SptRunTestSuites.Args, CommandFeedback> {

    private final SptClassLoaderResources classLoaderResources;
    private final SptCheckForOutputAggregator checkForOutputAggregator;

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
            return "Args{" + "directory=" + directory + ", rootDir=" + rootDir + '}';
        }
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public CommandFeedback exec(ExecContext context, Args input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        MultiTestSuiteRun result = context.require(checkForOutputAggregator, new SptCheckForOutputAggregator.Input(input.directory, input.rootDir));
        return CommandFeedback.of(ShowFeedback.showTests(result));
    }

    @Inject
    public SptRunTestSuites(SptClassLoaderResources classLoaderResources, SptCheckForOutputAggregator checkForOutputAggregator) {
        this.classLoaderResources = classLoaderResources;
        this.checkForOutputAggregator = checkForOutputAggregator;
    }
}
