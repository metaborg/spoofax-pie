package mb.spoofax.core.language.command.arg;

import mb.common.region.Region;
import mb.common.region.Selection;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.ResourcePathWithKind;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;

public class RawArgFromProviders {
    public static @Nullable Serializable get(Param param, CommandContext context) {
        for(ArgProvider provider : param.getProviders()) {
            @SuppressWarnings("ConstantConditions") final @Nullable Serializable arg = ArgProviders.caseOf(provider)
                .value((o) -> o)
                .context_(fromContext(param.getType(), context));
            // noinspection ConstantConditions (arg can really be null)
            if(arg != null) return arg;
        }
        return null;
    }

    private static @Nullable Serializable fromContext(Class<? extends Serializable> type, CommandContext context) {
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
}
