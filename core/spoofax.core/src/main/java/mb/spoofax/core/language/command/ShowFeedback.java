package mb.spoofax.core.language.command;

import mb.common.region.Region;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class ShowFeedback implements Serializable {
    public interface Cases<R> {
        R showFile(ResourceKey file, @Nullable Region region);

        R showText(String text, String name, @Nullable Region region);
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


    public abstract <R> R match(Cases<R> cases);

    public ShowFeedbacks.CaseOfMatchers.TotalMatcher_ShowFile caseOf() {
        return ShowFeedbacks.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
