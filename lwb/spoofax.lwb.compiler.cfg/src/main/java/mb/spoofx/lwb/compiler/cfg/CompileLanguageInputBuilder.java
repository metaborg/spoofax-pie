package mb.spoofx.lwb.compiler.cfg;

import mb.common.util.Properties;
import mb.spoofax.compiler.util.Shared;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileEsvInput;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileSdf3Input;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileStatixInput;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileStrategoInput;
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
        final @Nullable CompileSdf3Input sdf3 = buildSdf3(persistentProperties, shared, compileLanguageShared);
        if(sdf3 != null) compileLanguage.sdf3(sdf3);

        final @Nullable CompileEsvInput esv = buildEsv(compileLanguageShared);
        if(esv != null) compileLanguage.esv(esv);

        final @Nullable CompileStatixInput statix = buildStatix(compileLanguageShared);
        if(statix != null) compileLanguage.statix(statix);

        final @Nullable CompileStrategoInput stratego = buildStratego(compileLanguageShared, sdf3);
        if(stratego != null) compileLanguage.stratego(stratego);

        return compileLanguage
            .compileLanguageShared(compileLanguageShared)
            .build();
    }


    private @Nullable CompileSdf3Input buildSdf3(
        Properties persistentProperties,
        Shared shared,
        CompileLanguageShared compileLanguageShared
    ) {
        if(!sdf3Enabled) return null;
        return sdf3
            .withPersistentProperties(persistentProperties)
            .shared(shared)
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
        CompileLanguageShared compileLanguageShared,
        CompileSdf3Input parserInput
    ) {
        if(!strategoEnabled) return null;

        // Set required parts.
        stratego
            .compileLanguageShared(compileLanguageShared)
        ;

        final CompileStrategoInput.Builder builder;
        if(parserInput != null) {
            // Copy the builder before syncing to ensure that multiple builds do not cause a sync multiple times.
            builder = CompileStrategoInput.builder().from(stratego.build());
            parserInput.syncTo(builder);
        } else {
            builder = stratego;
        }

        return builder.build();
    }
}
