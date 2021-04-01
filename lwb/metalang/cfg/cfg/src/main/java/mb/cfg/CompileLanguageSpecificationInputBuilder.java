package mb.cfg;

import mb.cfg.metalang.CompileEsvInput;
import mb.cfg.metalang.CompileSdf3Input;
import mb.cfg.metalang.CompileStatixInput;
import mb.cfg.metalang.CompileStrategoInput;
import mb.common.util.Properties;
import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade for consistently building a {@link CompileLanguageSpecificationInput} instance.
 */
public class CompileLanguageSpecificationInputBuilder {
    private boolean sdf3Enabled = false;
    public CompileSdf3Input.Builder sdf3 = CompileSdf3Input.builder();

    private boolean esvEnabled = false;
    public CompileEsvInput.Builder esv = CompileEsvInput.builder();

    private boolean statixEnabled = false;
    public CompileStatixInput.Builder statix = CompileStatixInput.builder();

    private boolean strategoEnabled = false;
    public CompileStrategoInput.Builder stratego = CompileStrategoInput.builder();

    public CompileLanguageSpecificationInput.Builder compileLanguage = CompileLanguageSpecificationInput.builder();


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


    public CompileLanguageSpecificationInput build(Properties persistentProperties, Shared shared, CompileLanguageSpecificationShared compileLanguageSpecificationShared) {
        final @Nullable CompileSdf3Input sdf3 = buildSdf3(compileLanguageSpecificationShared);
        if(sdf3 != null) compileLanguage.sdf3(sdf3);

        final @Nullable CompileEsvInput esv = buildEsv(compileLanguageSpecificationShared);
        if(esv != null) compileLanguage.esv(esv);

        final @Nullable CompileStatixInput statix = buildStatix(compileLanguageSpecificationShared);
        if(statix != null) compileLanguage.statix(statix);

        final @Nullable CompileStrategoInput stratego = buildStratego(persistentProperties, shared, compileLanguageSpecificationShared);
        if(stratego != null) compileLanguage.stratego(stratego);

        return compileLanguage
            .compileLanguageShared(compileLanguageSpecificationShared)
            .build();
    }


    private @Nullable CompileSdf3Input buildSdf3(
        CompileLanguageSpecificationShared compileLanguageSpecificationShared
    ) {
        if(!sdf3Enabled) return null;
        return sdf3
            .compileLanguageShared(compileLanguageSpecificationShared)
            .build();
    }

    private @Nullable CompileEsvInput buildEsv(
        CompileLanguageSpecificationShared compileLanguageSpecificationShared
    ) {
        if(!esvEnabled) return null;
        return esv
            .compileLanguageShared(compileLanguageSpecificationShared)
            .build();
    }

    private @Nullable CompileStatixInput buildStatix(
        CompileLanguageSpecificationShared compileLanguageSpecificationShared
    ) {
        if(!statixEnabled) return null;
        return statix
            .compileLanguageShared(compileLanguageSpecificationShared)
            .build();
    }

    private @Nullable CompileStrategoInput buildStratego(
        Properties persistentProperties,
        Shared shared,
        CompileLanguageSpecificationShared compileLanguageSpecificationShared
    ) {
        if(!strategoEnabled) return null;
        return stratego
            .withPersistentProperties(persistentProperties)
            .shared(shared)
            .compileLanguageShared(compileLanguageSpecificationShared)
            .build();
    }
}
