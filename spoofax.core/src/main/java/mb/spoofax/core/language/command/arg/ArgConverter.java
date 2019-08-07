package mb.spoofax.core.language.command.arg;

import java.io.Serializable;

public interface ArgConverter<T extends Serializable> {
    T convert(String argStr) throws Exception;
}
