package mb.spoofax.core.language.transform.param;

public interface ArgConverter<T> {
    T convert(String argStr) throws Exception;
}
