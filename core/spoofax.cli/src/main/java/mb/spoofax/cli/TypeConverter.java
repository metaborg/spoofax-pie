package mb.spoofax.cli;

import mb.spoofax.core.language.command.arg.ArgConverter;
import picocli.CommandLine;

import java.io.Serializable;

class TypeConverter<T extends Serializable> implements CommandLine.ITypeConverter<T> {
    private final ArgConverter<T> converter;

    TypeConverter(ArgConverter<T> converter) {
        this.converter = converter;
    }

    @Override public T convert(String value) throws Exception {
        return converter.convert(value);
    }

    Class<T> getOutputClass() {
        return converter.getOutputClass();
    }
}
