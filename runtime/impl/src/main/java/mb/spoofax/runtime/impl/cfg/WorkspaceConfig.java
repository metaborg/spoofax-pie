package mb.spoofax.runtime.impl.cfg;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.immutables.serial.Serial;
import org.immutables.value.Value;

import mb.spoofax.runtime.model.SpoofaxEx;

@Value.Immutable
@Serial.Version(value = 1L)
public interface WorkspaceConfig extends Serializable {
    Map<String, LangSpecConfig> langSpecConfigPerExt();

    Map<String, SpxCoreConfig> spxCoreConfigPerExt();

    Set<String> extensions();


    public static WorkspaceConfig fromConfigs(Iterable<LangSpecConfig> langSpecConfigs,
        Iterable<SpxCoreConfig> spxCoreConfigs) throws SpoofaxEx {
        final ImmutableWorkspaceConfig.Builder builder = ImmutableWorkspaceConfig.builder();
        final Set<String> extensions = new HashSet<>();
        for(LangSpecConfig config : langSpecConfigs) {
            for(String extension : config.extensions()) {
                if(extensions.contains(extension)) {
                    throw new SpoofaxEx("Cannot add config '" + config + "' with extension '" + extension
                        + "', that extension is already in use");
                }
                extensions.add(extension);
                builder.putLangSpecConfigPerExt(extension, config);
            }
        }
        for(SpxCoreConfig config : spxCoreConfigs) {
            for(String extension : config.extensions()) {
                if(extensions.contains(extension)) {
                    throw new SpoofaxEx("Cannot add config '" + config + "' with extension '" + extension
                        + "', that extension is already in use");
                }
                extensions.add(extension);
                builder.putSpxCoreConfigPerExt(extension, config);
            }
        }
        builder.addAllExtensions(extensions);
        return builder.build();
    }


    public default Collection<LangSpecConfig> langSpecConfigs() {
        return langSpecConfigPerExt().values();
    }

    public default Collection<SpxCoreConfig> spxCoreConfigs() {
        return spxCoreConfigPerExt().values();
    }


    public default Set<String> langSpecExtensions() {
        return langSpecConfigPerExt().keySet();
    }

    public default Set<String> spxCoreExtensions() {
        return spxCoreConfigPerExt().keySet();
    }


    public default @Nullable LangSpecConfig langSpecConfigForExt(String extension) {
        return langSpecConfigPerExt().get(extension);
    }

    public default @Nullable SpxCoreConfig spxCoreConfigForExt(String extension) {
        return spxCoreConfigPerExt().get(extension);
    }
}
