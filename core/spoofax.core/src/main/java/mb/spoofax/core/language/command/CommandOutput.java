package mb.spoofax.core.language.command;

import mb.common.util.ListView;

import java.io.Serializable;
import java.util.Objects;

public class CommandOutput implements Serializable {
    public final ListView<CommandFeedback> feedback;

    public CommandOutput(ListView<CommandFeedback> feedback) {
        this.feedback = feedback;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CommandOutput that = (CommandOutput) o;
        return feedback.equals(that.feedback);
    }

    @Override public int hashCode() {
        return Objects.hash(feedback);
    }

    @Override public String toString() {
        return feedback.toString();
    }
}
