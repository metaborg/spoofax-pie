package mb.spoofax.runtime.cfg;

import mb.spoofax.api.SpoofaxEx;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

@Value.Immutable
@Serial.Version(value = 1L)
public interface WorkspaceConfig extends Serializable {
    List<LangSpecConfig> langSpecConfigs();


    static WorkspaceConfig fromConfigs(Iterable<LangSpecConfig> langSpecConfigs)
        throws SpoofaxEx {
        final ImmutableWorkspaceConfig.Builder builder = ImmutableWorkspaceConfig.builder();
        final Set<LangId> langIds = new HashSet<>();
        final Set<String> extensions = new HashSet<>();
        for(LangSpecConfig config : langSpecConfigs) {
            builder.addLangSpecConfigs(config);
            // Check language identifier
            final LangId langId = config.id();
            if(langIds.contains(langId)) {
                throw new SpoofaxEx("Cannot add config '" + config + "' with language identifier '" + langId
                    + "', that identifier is already in use");
            }
            langIds.add(config.id());
            // Check extensions
            for(String extension : config.extensions()) {
                if(extensions.contains(extension)) {
                    throw new SpoofaxEx("Cannot add config '" + config + "' with extension '" + extension
                        + "', that extension is already in use");
                }
                extensions.add(extension);
            }
        }
        return builder.build();
    }


    @Value.Lazy default Map<LangId, LangSpecConfig> langSpecConfigPerId() {
        final Map<LangId, LangSpecConfig> map = new HashMap<>();
        for(LangSpecConfig config : langSpecConfigs()) {
            map.put(config.id(), config);
        }
        return map;
    }

    @Value.Lazy default Set<LangId> langIds() {
        return langSpecConfigPerId().keySet();
    }

    default @Nullable LangSpecConfig langSpecConfigForId(LangId langId) {
        return langSpecConfigPerId().get(langId);
    }


    @Value.Lazy default Map<String, LangSpecConfig> langSpecConfigPerExt() {
        final Map<String, LangSpecConfig> map = new HashMap<>();
        for(LangSpecConfig config : langSpecConfigs()) {
            for(String extension : config.extensions()) {
                map.put(extension, config);
            }
        }
        return map;
    }

    @Value.Lazy default Set<String> extensions() {
        return langSpecConfigPerExt().keySet();
    }

    default @Nullable LangSpecConfig langSpecConfigForExt(String extension) {
        return langSpecConfigPerExt().get(extension);
    }
}
