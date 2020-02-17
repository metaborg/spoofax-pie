package mb.spoofax.core.language.command;

import mb.common.region.Region;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.cli.CliParams;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class CommandFeedback implements Serializable {
    public interface Cases<R> {
        R showFile(ResourceKey file, @Nullable Region region);

        R showText(String text, String name, @Nullable Region region);
    }

    public static CommandFeedback showFile(ResourceKey file, @Nullable Region region) {
        return CommandFeedbacks.showFile(file, region);
    }

    public static CommandFeedback showFile(ResourceKey file) {
        return CommandFeedbacks.showFile(file, null);
    }

    public static CommandFeedback showText(String text, String name, @Nullable Region region) {
        return CommandFeedbacks.showText(text, name, region);
    }

    public static CommandFeedback showText(String text, String name) {
        return CommandFeedbacks.showText(text, name, null);
    }


    public abstract <R> R match(Cases<R> cases);

    public CommandFeedbacks.CaseOfMatchers.TotalMatcher_ShowFile caseOf() {
        return CommandFeedbacks.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
