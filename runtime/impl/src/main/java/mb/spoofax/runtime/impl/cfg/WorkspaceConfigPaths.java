package mb.spoofax.runtime.impl.cfg;

import static mb.spoofax.runtime.impl.term.Terms.*;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.spoofax.runtime.impl.term.Terms;
import mb.vfs.path.PPath;

@Value.Immutable
@Serial.Version(value = 1L)
public interface WorkspaceConfigPaths extends Serializable {
    @Value.Parameter List<PPath> langSpecConfigFiles();

    @Value.Parameter List<PPath> spxCoreLangConfigFiles();

    @Value.Parameter List<PPath> spxCoreLangSpecConfigFiles();


    public static WorkspaceConfigPaths fromTerm(IStrategoTerm root, PPath dir) {
        // @formatter:off
        // Sections([...])
        final IStrategoTerm rootSections = root.getSubterm(0);
        // Sections([WorkspaceSec([...])])
        final List<IStrategoTerm> workspaceSection = Terms
            .stream(rootSections)
            .filter((t) -> Terms.isAppl(t, "WorkspaceSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        final List<PPath> langSpecConfigFiles = workspaceSection
            .stream()
            .filter((t) -> isAppl(t, "LangSpec")) // LangSpec(Path("...))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map((s) -> dir.resolve(s))
            .collect(Collectors.toList());
        final List<PPath> spxCoreLangConfigFiles = workspaceSection
            .stream()
            .filter((t) -> isAppl(t, "SpxLang")) // SpxLang(Path("...))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map((s) -> dir.resolve(s))
            .collect(Collectors.toList());
        final List<PPath> spxCoreLangSpecConfigFiles = workspaceSection
            .stream()
            .filter((t) -> isAppl(t, "SpxLangSpec")) // SpxLangSpec(Path("...))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map((s) -> dir.resolve(s))
            .collect(Collectors.toList());
        // @formatter:on

        return ImmutableWorkspaceConfigPaths.of(langSpecConfigFiles, spxCoreLangConfigFiles,
            spxCoreLangSpecConfigFiles);
    }
}
