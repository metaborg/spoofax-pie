package mb.spoofax.core.language.command;

import mb.common.util.ListView;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CommandOutput implements Serializable {
    public final ListView<CommandFeedback> feedback;

    public CommandOutput(ListView<CommandFeedback> feedback) {
        this.feedback = feedback;
    }

    public static CommandOutput of() {
        return new CommandOutput(ListView.of());
    }

    public static CommandOutput of(CommandFeedback feedback) {
        return new CommandOutput(ListView.of(feedback));
    }

    public static CommandOutput of(CommandFeedback... feedbacks) {
        return new CommandOutput(ListView.of(feedbacks));
    }

    public static CommandOutput of(List<? extends CommandFeedback> feedbacks) {
        return new CommandOutput(ListView.of(feedbacks));
    }

    public static CommandOutput copyOf(Iterable<? extends CommandFeedback> feedbacks) {
        return new CommandOutput(ListView.copyOf(feedbacks));
    }

    public static CommandOutput copyOf(Collection<? extends CommandFeedback> feedbacks) {
        return new CommandOutput(ListView.copyOf(feedbacks));
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CommandOutput that = (CommandOutput)o;
        return feedback.equals(that.feedback);
    }

    @Override public int hashCode() {
        return Objects.hash(feedback);
    }

    @Override public String toString() {
        return feedback.toString();
    }
}
