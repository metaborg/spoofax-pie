package mb.spoofax.core.language.command;

import mb.common.region.Region;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.testrunner.TestResults;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;

@ADT
public abstract class ShowFeedback implements Serializable {
    public interface Cases<R> {
        R showFile(ResourceKey file, @Nullable Region region);

        R showText(String text, String name, @Nullable Region region);

        R showTestResults(TestResults testResults, @Nullable Region region);
    }

    @SuppressWarnings("ConstantConditions")
    public static ShowFeedback showFile(ResourceKey file, @Nullable Region region) {
        return ShowFeedbacks.showFile(file, region);
    }

    @SuppressWarnings("ConstantConditions")
    public static ShowFeedback showFile(ResourceKey file) {
        return ShowFeedbacks.showFile(file, null);
    }

    @SuppressWarnings("ConstantConditions")
    public static ShowFeedback showText(String text, String name, @Nullable Region region) {
        return ShowFeedbacks.showText(text, name, region);
    }

    @SuppressWarnings("ConstantConditions")
    public static ShowFeedback showText(String text, String name) {
        return ShowFeedbacks.showText(text, name, null);
    }

    @SuppressWarnings("ConstantConditions")
    public static ShowFeedback showTestResults(TestResults testResults) {
        return ShowFeedbacks.showTestResults(testResults, null);
    }


    public abstract <R> R match(Cases<R> cases);

    public Optional<ResourceKey> getFile() {
        return ShowFeedbacks.getFile(this);
    }

    public Optional<String> getName() {
        return ShowFeedbacks.getName(this);
    }

    public Optional<String> getText() {
        return ShowFeedbacks.getText(this);
    }

    @SuppressWarnings("ConstantConditions") public Optional<Region> getRegion() {
        final @Nullable Region region = ShowFeedbacks.getRegion(this);
        return Optional.ofNullable(region);
    }

    public Optional<TestResults> getTestResults() {
        return ShowFeedbacks.getTestResults(this);
    }

    public static ShowFeedbacks.CasesMatchers.TotalMatcher_ShowFile cases() {
        return ShowFeedbacks.cases();
    }

    public ShowFeedbacks.CaseOfMatchers.TotalMatcher_ShowFile caseOf() {
        return ShowFeedbacks.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override @SuppressWarnings("ConstantConditions") public String toString() {
        return cases()
            .showFile((file, region) -> "show file '" + file + "'" + (region != null ? " @" + region : ""))
            .showText((text, name, region) -> "show text (" + text.length() + " chars) with name '" + name + "'" + (region != null ? " @" + region : ""))
            .showTestResults((testResults, region) -> "show test results (" + testResults.numPassed + " passed, " + testResults.numFailed + " failed)" + (region != null ? " @" + region : ""))
            .apply(this);
    }
}
