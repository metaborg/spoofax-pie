package mb.spoofax.runtime.impl.cfg;

import mb.spoofax.runtime.model.SpoofaxEx;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

@Value.Immutable
@Serial.Version(value = 1L)
public interface WorkspaceConfig extends Serializable {
    List<LangSpecConfig> langSpecConfigs();

    List<SpxCoreConfig> spxCoreConfigs();


    @Value.Lazy default Map<String, LangSpecConfig> langSpecConfigPerExt() {
        final Map<String, LangSpecConfig> map = new HashMap<>();
        for(LangSpecConfig config : langSpecConfigs()) {
            for(String extension : config.extensions()) {
                map.put(extension, config);
            }
        }
        return map;
    }

    @Value.Lazy default Map<String, SpxCoreConfig> spxCoreConfigPerExt() {
        final Map<String, SpxCoreConfig> map = new HashMap<>();
        for(SpxCoreConfig config : spxCoreConfigs()) {
            for(String extension : config.extensions()) {
                map.put(extension, config);
            }
        }
        return map;
    }

    @Value.Lazy default Set<String> extensions() {
        final Set<String> extensions = new HashSet<>();
        extensions.addAll(langSpecConfigPerExt().keySet());
        extensions.addAll(spxCoreConfigPerExt().keySet());
        return extensions;
    }


    static WorkspaceConfig fromConfigs(Iterable<LangSpecConfig> langSpecConfigs, Iterable<SpxCoreConfig> spxCoreConfigs)
        throws SpoofaxEx {
        final ImmutableWorkspaceConfig.Builder builder = ImmutableWorkspaceConfig.builder();
        final Set<String> extensions = new HashSet<>();
        for(LangSpecConfig config : langSpecConfigs) {
            builder.addLangSpecConfigs(config);
            for(String extension : config.extensions()) {
                if(extensions.contains(extension)) {
                    throw new SpoofaxEx("Cannot add config '" + config + "' with extension '" + extension
                        + "', that extension is already in use");
                }
                extensions.add(extension);
            }
        }
        for(SpxCoreConfig config : spxCoreConfigs) {
            builder.addSpxCoreConfigs(config);
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


    default Set<String> langSpecExtensions() {
        return langSpecConfigPerExt().keySet();
    }

    default Set<String> spxCoreExtensions() {
        return spxCoreConfigPerExt().keySet();
    }


    default @Nullable LangSpecConfig langSpecConfigForExt(String extension) {
        return langSpecConfigPerExt().get(extension);
    }

    default @Nullable SpxCoreConfig spxCoreConfigForExt(String extension) {
        return spxCoreConfigPerExt().get(extension);
    }
}
