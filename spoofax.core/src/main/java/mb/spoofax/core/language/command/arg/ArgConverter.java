package mb.spoofax.core.language.command.arg;

public interface ArgConverter<T> {
    T convert(String argStr) throws Exception;
}
