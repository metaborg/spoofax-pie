package mb.spoofax.core.language.command.arg;

import mb.common.util.MapView;
import mb.spoofax.core.language.command.CommandContext;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RawArgsBuilder {
    private final ParamDef paramDef;
    private final ArgConverters argConverters;

    private final HashMap<Class<? extends Serializable>, ArgConverter<?>> converters = new HashMap<>();
    private final HashMap<String, Serializable> args = new HashMap<>();


    public RawArgsBuilder(ParamDef paramDef, ArgConverters argConverters) {
        this.paramDef = paramDef;
        this.argConverters = argConverters;
    }


    public void setArg(String paramId, Serializable arg) {
        args.put(paramId, arg);
    }

    public void setArgsFrom(RawArgs args) {
        for(Map.Entry<String, Serializable> optionArg : args.args) {
            this.args.put(optionArg.getKey(), optionArg.getValue());
        }
    }

    public void setConverter(Class<? extends Serializable> type, ArgConverter<?> converter) {
        converters.put(type, converter);
    }


    public RawArgs build(CommandContext context) {
        final HashMap<String, Serializable> convertedArgs = new HashMap<>();
        for(Param param : paramDef.params.values()) {
            final String id = param.getId();
            final Class<? extends Serializable> type = param.getType();
            final boolean isRequired = param.isRequired();
            final @Nullable ArgConverter<?> converter = param.getConverter();

            @Nullable Serializable arg = args.get(id);
            if(arg == null) {
                arg = RawArgFromProviders.get(param, context);
            }
            final boolean argSet = arg != null;
            if(!argSet && isRequired) {
                throw new RuntimeException("Parameter '" + id + "' of type '" + type + "' is required, but no argument was set, and no argument could be retrieved from providers '" + param.getProviders() + "'");
            }
            if(argSet) {
                if(String.class.equals(arg.getClass()) && !String.class.isAssignableFrom(type)) {
                    arg = convert((String)arg, type, converter);
                }
                convertedArgs.put(id, arg);
            }
        }
        return new RawArgs(new MapView<>(convertedArgs));
    }


    private Serializable convert(String argStr, Class<? extends Serializable> type, @Nullable ArgConverter<?> customConverter) {
        @Nullable ArgConverter<?> converter = customConverter != null ? customConverter : converters.get(type);
        if(converter == null) {
            converter = argConverters.allConverters.get(type);
            if(converter == null) {
                throw new RuntimeException("Cannot convert argument '" + argStr + "' to an object of type '" + type + "', no type converter was found for that type");
            }
        }
        try {
            return converter.convert(argStr);
        } catch(RuntimeException e) {
            throw e; // Just rethrow runtime exceptions.
        } catch(Exception e) {
            throw new RuntimeException("Cannot convert argument '" + argStr + "' to an object of type '" + type + "', conversion failed unexpectedly", e);
        }
    }
}
