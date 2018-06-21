package mb.spoofax.legacy;

import org.metaborg.core.language.ILanguageComponent;

public class LoadedMetaLanguages {
    public final ILanguageComponent config;
    public final ILanguageComponent spoofaxLib;
    public final ILanguageComponent esv;
    public final ILanguageComponent stratego;
    public final ILanguageComponent sdf3;
    public final ILanguageComponent nabl2Lang;
    public final ILanguageComponent nabl2Shared;
    public final ILanguageComponent nabl2Runtime;

    public LoadedMetaLanguages(ILanguageComponent config, ILanguageComponent spoofaxLib, ILanguageComponent esv, ILanguageComponent stratego,
        ILanguageComponent sdf3, ILanguageComponent nabl2Lang, ILanguageComponent nabl2Shared, ILanguageComponent nabl2Runtime) {
        this.config = config;
        this.spoofaxLib = spoofaxLib;
        this.esv = esv;
        this.stratego = stratego;
        this.sdf3 = sdf3;
        this.nabl2Lang = nabl2Lang;
        this.nabl2Shared = nabl2Shared;
        this.nabl2Runtime = nabl2Runtime;
    }
}
