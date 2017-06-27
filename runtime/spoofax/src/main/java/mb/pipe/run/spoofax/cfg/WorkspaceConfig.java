package mb.pipe.run.spoofax.cfg;

import java.io.Serializable;
import java.util.*;

import javax.annotation.Nullable;

import mb.pipe.run.core.PipeRunEx;

public class WorkspaceConfig implements Serializable {
    private static final long serialVersionUID = 2L;

    private final Map<String, LangSpecConfig> langSpecConfigPerExt = new HashMap<>();
    private final Map<String, SpxCoreLangConfig> spxCoreLangConfigPerExt = new HashMap<>();
    private final Map<String, SpxCoreLangSpecConfig> spxCoreLangSpecConfigPerExt = new HashMap<>();
    private final Set<String> extensions = new HashSet<>();


    public static WorkspaceConfig generate(Iterable<LangSpecConfig> langSpecConfigs,
        Iterable<SpxCoreLangConfig> spxCoreLangConfigs, Iterable<SpxCoreLangSpecConfig> spxCoreLangSpecConfigs) {
        final WorkspaceConfig workbenchConfig = new WorkspaceConfig();
        for(LangSpecConfig config : langSpecConfigs) {
            workbenchConfig.addConfig(config);
        }
        for(SpxCoreLangConfig config : spxCoreLangConfigs) {
            workbenchConfig.addConfig(config);
        }
        for(SpxCoreLangSpecConfig config : spxCoreLangSpecConfigs) {
            workbenchConfig.addConfig(config);
        }
        return workbenchConfig;
    }


    public Set<String> allExtensions() {
        return new HashSet<>(extensions);
    }

    public Set<String> langSpecExtensions() {
        return langSpecConfigPerExt.keySet();
    }

    public Set<String> spxCoreLangExtensions() {
        return spxCoreLangConfigPerExt.keySet();
    }

    public Set<String> spxCoreLangSpecExtensions() {
        return spxCoreLangSpecConfigPerExt.keySet();
    }


    public @Nullable LangSpecConfig langSpecConfigForExt(String extension) {
        return langSpecConfigPerExt.get(extension);
    }

    public @Nullable SpxCoreLangConfig spxCoreLangConfigForExt(String extension) {
        return spxCoreLangConfigPerExt.get(extension);
    }

    public @Nullable SpxCoreLangSpecConfig spxCoreLangSpecConfigForExt(String extension) {
        return spxCoreLangSpecConfigPerExt.get(extension);
    }


    public void addConfig(LangSpecConfig config) {
        for(String extension : config.extensions()) {
            if(extensions.contains(extension)) {
                throw new PipeRunEx("Cannot add config '" + config + "' with extension '" + extension
                    + "', that extension is already in use");
            }
            langSpecConfigPerExt.put(extension, config);
            extensions.add(extension);
        }
    }

    public void addConfig(SpxCoreLangConfig config) {
        final String extension = config.extension();
        if(extensions.contains(extension)) {
            throw new PipeRunEx("Cannot add config '" + config + "' with extension '" + extension
                + "', that extension is already in use");
        }
        spxCoreLangConfigPerExt.put(extension, config);
        extensions.add(extension);
    }

    public void addConfig(SpxCoreLangSpecConfig config) {
        final ArrayList<String> extensions = config.extensions();
        for(String extension : extensions) {
            if(extensions.contains(extension)) {
                throw new PipeRunEx("Cannot add config '" + config + "' with extension '" + extension
                    + "', that extension is already in use");
            }
            spxCoreLangSpecConfigPerExt.put(extension, config);
            extensions.add(extension);
        }
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + langSpecConfigPerExt.hashCode();
        result = prime * result + spxCoreLangConfigPerExt.hashCode();
        result = prime * result + spxCoreLangSpecConfigPerExt.hashCode();
        result = prime * result + extensions.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final WorkspaceConfig other = (WorkspaceConfig) obj;
        if(!langSpecConfigPerExt.equals(other.langSpecConfigPerExt))
            return false;
        if(!spxCoreLangConfigPerExt.equals(other.spxCoreLangConfigPerExt))
            return false;
        if(!spxCoreLangSpecConfigPerExt.equals(other.spxCoreLangSpecConfigPerExt))
            return false;
        if(!extensions.equals(other.extensions))
            return false;
        return true;
    }
}
