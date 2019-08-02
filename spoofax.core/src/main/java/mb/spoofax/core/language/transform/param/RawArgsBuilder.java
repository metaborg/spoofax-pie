package mb.spoofax.core.language.transform.param;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.transform.TransformContext;
import mb.spoofax.core.language.transform.TransformContexts;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class RawArgsBuilder {
    private final ParamDef paramDef;
    private final HashMap<Class<?>, ArgConverter<?>> converters = new HashMap<>();
    private final HashMap<String, Object> optionArgs = new HashMap<>();
    private final ArrayList<Object> positionalArgs = new ArrayList<>();


    public RawArgsBuilder(ParamDef paramDef) {
        this.paramDef = paramDef;
        // TODO: add default argument converters.
    }


    public void setConverter(Class<?> type, ArgConverter<?> converter) {
        converters.put(type, converter);
    }

    public void setOptionArg(String name, Object arg) {
        optionArgs.put(name, arg);
    }

    public void addPositionalArg(Object arg) {
        positionalArgs.add(arg);
    }


    public RawArgs build(TransformContext context) {
        final HashMap<String, Object> finalOptionArgs = new HashMap<>();
        final HashMap<Integer, Object> finalPositionalArgs = new HashMap<>();
        // TODO: use a list for positional arguments instead, but that requires looping over positionalArgs instead, and
        // then requires finding the matching positional parameters.

        for(Param param : paramDef.params) {
            Params.caseOf(param)
                .option((name, type, required, providers) -> {
                    @Nullable Object arg = optionArgs.get(name);
                    if(arg == null && !providers.isEmpty()) {
                        arg = argFromProviders(type, providers, context);
                    }
                    final boolean argSet = arg != null;
                    if(!argSet && required) {
                        throw new RuntimeException("Option parameter '" + name + "' of type '" + type + "' is required, but no argument was set, and no argument could be retrieved from providers '" + providers + "'");
                    }
                    if(argSet) {
                        if(String.class.equals(arg.getClass()) && !String.class.isAssignableFrom(type)) {
                            arg = convert((String) arg, type);
                        }
                        finalOptionArgs.put(name, arg);
                    }
                    return Optional.empty();
                })
                .positional((index, type, required, providers) -> {
                    @Nullable Object arg;
                    if(index < positionalArgs.size()) {
                        arg = positionalArgs.get(index);
                    } else {
                        arg = null;
                    }
                    if(arg == null && !providers.isEmpty()) {
                        arg = argFromProviders(type, providers, context);
                    }
                    final boolean argSet = arg != null;
                    if(!argSet && required) {
                        throw new RuntimeException("Positional parameter at index '" + index + "' of type '" + type + "' is required, but no argument was set, and no argument could be retrieved from providers '" + providers + "'");
                    }
                    if(argSet) {
                        if(String.class.equals(arg.getClass()) && !String.class.isAssignableFrom(type)) {
                            arg = convert((String) arg, type);
                        }
                        finalPositionalArgs.put(index, arg);
                    }
                    return Optional.empty();
                });
        }
        return new DefaultRawArgs(finalOptionArgs, finalPositionalArgs);
    }


    private @Nullable Object argFromProviders(Class<?> type, ListView<ArgProvider> providers, TransformContext context) {
        for(ArgProvider provider : providers) {
            @SuppressWarnings("ConstantConditions") final @Nullable Object arg = ArgProviders.caseOf(provider)
                .value((o) -> o)
                .context_(argFromContext(type, context));
            // noinspection ConstantConditions (arg can really be null)
            if(arg != null) return arg;
        }
        return null;
    }

    private @Nullable Object argFromContext(Class<?> type, TransformContext context) {
        if(TransformContext.class.isAssignableFrom(type)) {
            return context;
        } else if(Region.class.isAssignableFrom(type)) {
            return TransformContexts.getRegion(context).orElse(null);
        } else if(Integer.class.isAssignableFrom(type)) {
            return TransformContexts.getOffset(context).orElse(null);
        } else {
            if(ResourcePath.class.isAssignableFrom(type)) {
                final Optional<ResourcePath> path = TransformContexts.caseOf(context)
                    .project((p) -> p)
                    .directory((p) -> p)
                    .file((p) -> p)
                    .fileWithRegion((p, r) -> p)
                    .fileWithOffset((p, o) -> p)
                    .otherwiseEmpty();
                if(path.isPresent()) {
                    return path;
                }
            }
            if(ResourceKey.class.isAssignableFrom(type)) {
                return TransformContexts.getReadable(context).orElse(null);
            }
            return null;
        }
    }


    private Object convert(String argStr, Class<?> type) {
        final @Nullable ArgConverter<?> converter = converters.get(type);
        if(converter == null) {
            throw new RuntimeException("Cannot convert argument '" + argStr + "' to an object of type '" + type + "', no type converter was found for that type");
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
