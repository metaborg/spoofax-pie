package mb.cfg;

import mb.cfg.metalang.CompileEsvInput;
import mb.cfg.metalang.CompileSdf3Input;
import mb.cfg.metalang.CompileStatixInput;
import mb.cfg.metalang.CompileStrategoInput;
import mb.common.util.Properties;
import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade for consistently building a {@link CompileLanguageInput} instance.
 */
public class CompileLanguageInputBuilder {
    private boolean sdf3Enabled = false;
    public CompileSdf3Input.Builder sdf3 = CompileSdf3Input.builder();

    private boolean esvEnabled = false;
    public CompileEsvInput.Builder esv = CompileEsvInput.builder();

    private boolean statixEnabled = false;
    public CompileStatixInput.Builder statix = CompileStatixInput.builder();

    private boolean strategoEnabled = false;
    public CompileStrategoInput.Builder stratego = CompileStrategoInput.builder();

    public CompileLanguageInput.Builder compileLanguage = CompileLanguageInput.builder();


    public CompileSdf3Input.Builder withSdf3() {
        sdf3Enabled = true;
        return sdf3;
    }

    public CompileEsvInput.Builder withEsv() {
        esvEnabled = true;
        return esv;
    }

    public CompileStatixInput.Builder withStatix() {
        statixEnabled = true;
        return statix;
    }

    public CompileStrategoInput.Builder withStratego() {
        strategoEnabled = true;
        return stratego;
    }


    public CompileLanguageInput build(Properties persistentProperties, Shared shared, CompileLanguageShared compileLanguageShared) {
        final @Nullable CompileSdf3Input sdf3 = buildSdf3(compileLanguageShared);
        if(sdf3 != null) compileLanguage.sdf3(sdf3);

        final @Nullable CompileEsvInput esv = buildEsv(compileLanguageShared);
        if(esv != null) compileLanguage.esv(esv);

        final @Nullable CompileStatixInput statix = buildStatix(compileLanguageShared);
        if(statix != null) compileLanguage.statix(statix);

        final @Nullable CompileStrategoInput stratego = buildStratego(persistentProperties, shared, compileLanguageShared);
        if(stratego != null) compileLanguage.stratego(stratego);

        return compileLanguage
            .compileLanguageShared(compileLanguageShared)
            .build();
    }


    private @Nullable CompileSdf3Input buildSdf3(
        CompileLanguageShared compileLanguageShared
    ) {
        if(!sdf3Enabled) return null;
        return sdf3
            .compileLanguageShared(compileLanguageShared)
            .build();
    }

    private @Nullable CompileEsvInput buildEsv(
        CompileLanguageShared compileLanguageShared
    ) {
        if(!esvEnabled) return null;
        return esv
            .compileLanguageShared(compileLanguageShared)
            .build();
    }

    private @Nullable CompileStatixInput buildStatix(
        CompileLanguageShared compileLanguageShared
    ) {
        if(!statixEnabled) return null;
        return statix
            .compileLanguageShared(compileLanguageShared)
            .build();
    }

    private @Nullable CompileStrategoInput buildStratego(
        Properties persistentProperties,
        Shared shared,
        CompileLanguageShared compileLanguageShared
    ) {
        if(!strategoEnabled) return null;
        return stratego
            .withPersistentProperties(persistentProperties)
            .shared(shared)
            .compileLanguageShared(compileLanguageShared)
            .build();
    }
}
