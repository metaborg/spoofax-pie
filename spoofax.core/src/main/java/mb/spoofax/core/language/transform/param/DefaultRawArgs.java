package mb.spoofax.core.language.transform.param;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class DefaultRawArgs implements RawArgs {
    private final HashMap<String, Object> optionArgs;
    private final HashMap<Integer, Object> positionalArgs;

    public DefaultRawArgs(HashMap<String, Object> optionArgs, HashMap<Integer, Object> positionalArgs) {
        this.optionArgs = optionArgs;
        this.positionalArgs = positionalArgs;
    }

    @Override public <T> @Nullable T getOption(String name) {
        @SuppressWarnings("unchecked") final T arg = (T) optionArgs.get(name);
        return arg;
    }

    @Override public <T> @Nullable T getPositional(int index) {
        @SuppressWarnings("unchecked") final T arg = (T) positionalArgs.get(index);
        return arg;
    }
}
