package mb.spoofax.core.language.command.arg;

import mb.common.region.Region;
import mb.common.region.Selection;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.ResourcePathWithKind;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RawArgsBuilder {
    private final ParamDef paramDef;
    private final DefaultArgConverters defaultArgConverters;

    private final HashMap<Class<? extends Serializable>, ArgConverter<?>> converters = new HashMap<>();
    private final HashMap<String, Serializable> args = new HashMap<>();


    public RawArgsBuilder(ParamDef paramDef, DefaultArgConverters defaultArgConverters) {
        this.paramDef = paramDef;
        this.defaultArgConverters = defaultArgConverters;
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
            final ListView<ArgProvider> providers = param.getProviders();

            @Nullable Serializable arg = args.get(id);
            if(arg == null && !providers.isEmpty()) {
                arg = argFromProviders(type, providers, context);
            }
            final boolean argSet = arg != null;
            if(!argSet && isRequired) {
                throw new RuntimeException("Parameter '" + id + "' of type '" + type + "' is required, but no argument was set, and no argument could be retrieved from providers '" + providers + "'");
            }
            if(argSet) {
                if(String.class.equals(arg.getClass()) && !String.class.isAssignableFrom(type)) {
                    arg = convert((String) arg, type);
                }
                convertedArgs.put(id, arg);
            }
        }
        return new RawArgs(new MapView<>(convertedArgs));
    }


    private @Nullable Serializable argFromProviders(Class<? extends Serializable> type, ListView<ArgProvider> providers, CommandContext context) {
        for(ArgProvider provider : providers) {
            @SuppressWarnings("ConstantConditions") final @Nullable Serializable arg = ArgProviders.caseOf(provider)
                .value((o) -> o)
                .context_(argFromContext(type, context));
            // noinspection ConstantConditions (arg can really be null)
            if(arg != null) return arg;
        }
        return null;
    }

    private @Nullable Serializable argFromContext(Class<? extends Serializable> type, CommandContext context) {
        if(type.isAssignableFrom(CommandContext.class)) {
            return context;
        } else if(type.isAssignableFrom(Region.class)) {
            return context.getSelection().flatMap(Selection::getRegion).orElse(null);
        } else if(type.isAssignableFrom(Integer.class)) {
            return context.getSelection().flatMap(Selection::getOffset).orElse(null);
        } else {
            if(type.isAssignableFrom(ResourceKey.class)) {
                final Optional<ResourceKey> key = context.getResourceKey();
                if(key.isPresent()) {
                    return key.get();
                }
            } else if(type.isAssignableFrom(ResourcePath.class)) {
                final Optional<ResourcePath> path = context.getResourcePathWithKind().map(ResourcePathWithKind::getPath);
                if(path.isPresent()) {
                    return path.get();
                }
            }
            return null;
        }
    }


    private Serializable convert(String argStr, Class<? extends Serializable> type) {
        @Nullable ArgConverter<?> converter = converters.get(type);
        if(converter == null) {
            converter = defaultArgConverters.allConverters.get(type);
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
