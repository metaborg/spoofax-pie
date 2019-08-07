package mb.spoofax.core.language.command;

import java.io.Serializable;
import java.util.Objects;

public class CommandInput<A extends Serializable> implements Serializable {
    public final A args;

    public CommandInput(A args) {
        this.args = args;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CommandInput that = (CommandInput) o;
        return args.equals(that.args);
    }

    @Override public int hashCode() {
        return Objects.hash(args);
    }

    @Override public String toString() {
        return args.toString();
    }
}
