package mb.pipe.run.spoofax.cfg;

import static mb.pipe.run.spoofax.term.Terms.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pipe.run.core.PipeRunEx;
import mb.pipe.run.core.path.PPath;
import mb.pipe.run.spoofax.term.Terms;

public class WorkspaceConfig implements Serializable {
    private static final long serialVersionUID = 3L;

    private final HashMap<String, LangSpecConfig> langSpecConfigPerExt = new HashMap<>();
    private final HashMap<String, SpxCoreConfig> spxCoreConfigPerExt = new HashMap<>();
    private final HashSet<String> extensions = new HashSet<>();


    public static class WorkspaceConfigPaths {
        public final ArrayList<PPath> langSpecConfigFiles;
        public final ArrayList<PPath> spxCoreLangConfigFiles;
        public final ArrayList<PPath> spxCoreLangSpecConfigFiles;


        public WorkspaceConfigPaths(IStrategoTerm root, PPath dir) {
            // @formatter:off
            // Sections([...])
            final IStrategoTerm rootSections = root.getSubterm(0);
            // Sections([WorkspaceSec([...])])
            final List<IStrategoTerm> workspaceSection = Terms
                .stream(rootSections)
                .filter((t) -> Terms.isAppl(t, "WorkspaceSec"))
                .flatMap((t) -> stream(t.getSubterm(0)))
                .collect(Collectors.toList());
            this.langSpecConfigFiles = workspaceSection
                .stream()
                .filter((t) -> isAppl(t, "LangSpec")) // LangSpec(Path("...))
                .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
                .map((s) -> dir.resolve(s))
                .collect(Collectors.toCollection(ArrayList::new));
            this.spxCoreLangConfigFiles = workspaceSection
                .stream()
                .filter((t) -> isAppl(t, "SpxLang")) // SpxLang(Path("...))
                .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
                .map((s) -> dir.resolve(s))
                .collect(Collectors.toCollection(ArrayList::new));
            this.spxCoreLangSpecConfigFiles = workspaceSection
                .stream()
                .filter((t) -> isAppl(t, "SpxLangSpec")) // SpxLangSpec(Path("...))
                .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
                .map((s) -> dir.resolve(s))
                .collect(Collectors.toCollection(ArrayList::new));
            // @formatter:on
        }
    }


    public WorkspaceConfig(Iterable<LangSpecConfig> langSpecConfigs, Iterable<SpxCoreConfig> spxCoreConfigs) {
        for(LangSpecConfig config : langSpecConfigs) {
            addConfig(config);
        }
        for(SpxCoreConfig config : spxCoreConfigs) {
            addConfig(config);
        }
    }


    public Set<String> allExtensions() {
        return new HashSet<>(extensions);
    }

    public Set<String> langSpecExtensions() {
        return langSpecConfigPerExt.keySet();
    }

    public Set<String> spxCoreExtensions() {
        return spxCoreConfigPerExt.keySet();
    }


    public @Nullable LangSpecConfig langSpecConfigForExt(String extension) {
        return langSpecConfigPerExt.get(extension);
    }

    public @Nullable SpxCoreConfig spxCoreConfigForExt(String extension) {
        return spxCoreConfigPerExt.get(extension);
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

    public void addConfig(SpxCoreConfig config) {
        for(String extension : config.extensions()) {
            if(extensions.contains(extension)) {
                throw new PipeRunEx("Cannot add config '" + config + "' with extension '" + extension
                    + "', that extension is already in use");
            }
            spxCoreConfigPerExt.put(extension, config);
            extensions.add(extension);
        }
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + langSpecConfigPerExt.hashCode();
        result = prime * result + spxCoreConfigPerExt.hashCode();
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
        if(!spxCoreConfigPerExt.equals(other.spxCoreConfigPerExt))
            return false;
        if(!extensions.equals(other.extensions))
            return false;
        return true;
    }
}
