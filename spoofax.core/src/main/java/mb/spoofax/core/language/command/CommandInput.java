package mb.spoofax.core.language.command;

import java.io.Serializable;
import java.util.Objects;

public class CommandInput<A extends Serializable> implements Serializable {
    public final A arguments;

    public CommandInput(A arguments) {
        this.arguments = arguments;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CommandInput that = (CommandInput) o;
        return arguments.equals(that.arguments);
    }

    @Override public int hashCode() {
        return Objects.hash(arguments);
    }

    @Override public String toString() {
        return arguments.toString();
    }
}
