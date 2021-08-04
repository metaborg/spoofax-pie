package mb.spt.task;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.spoofax.core.language.testrunner.TestResults;
import mb.spt.SptClassLoaderResources;
import mb.spoofax.core.language.testrunner.TestSuiteResult;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

public class SptShowTestSuiteResults implements TaskDef<SptShowTestSuiteResults.Args, CommandFeedback> {

    private final SptClassLoaderResources classLoaderResources;
    private final SptRunTestSuite checkForOutput;

    public static class Args implements Serializable {
        public final ResourcePath rootDir;
        public final ResourceKey file;

        public Args(ResourcePath rootDir, ResourceKey file) {
            this.rootDir = rootDir;
            this.file = file;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return file.equals(args.file) && rootDir.equals(args.rootDir);
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, rootDir);
        }

        @Override
        public String toString() {
            return "Args{" + "file=" + file + ", rootDir=" + rootDir + '}';
        }
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public CommandFeedback exec(ExecContext context, Args input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        TestResults totalResult = new TestResults();
        TestSuiteResult result = context.require(checkForOutput, new SptRunTestSuite.Input(input.file, input.rootDir));
        totalResult.add(result);
        return CommandFeedback.of(ShowFeedback.showTestResults(totalResult));
    }

    @Inject
    public SptShowTestSuiteResults(SptClassLoaderResources classLoaderResources, SptRunTestSuite checkForOutput) {
        this.classLoaderResources = classLoaderResources;
        this.checkForOutput = checkForOutput;
    }
}
