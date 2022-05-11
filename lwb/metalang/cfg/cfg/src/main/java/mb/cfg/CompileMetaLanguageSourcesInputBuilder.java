package mb.cfg;

import mb.cfg.metalang.CfgDynamixConfig;
import mb.cfg.metalang.CfgEsvConfig;
import mb.cfg.metalang.CfgSdf3Config;
import mb.cfg.metalang.CfgStatixConfig;
import mb.cfg.metalang.CfgStrategoConfig;
import mb.common.util.Properties;
import mb.spoofax.compiler.util.Shared;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade for consistently building a {@link CompileMetaLanguageSourcesInput} instance.
 */
public class CompileMetaLanguageSourcesInputBuilder {
    private boolean sdf3Enabled = false;
    public CfgSdf3Config.Builder sdf3 = CfgSdf3Config.builder();

    private boolean esvEnabled = false;
    public CfgEsvConfig.Builder esv = CfgEsvConfig.builder();

    private boolean statixEnabled = false;
    public CfgStatixConfig.Builder statix = CfgStatixConfig.builder();

    private boolean dynamixEnabled = false;
    public CfgDynamixConfig.Builder dynamix = CfgDynamixConfig.builder();

    private boolean strategoEnabled = false;
    public CfgStrategoConfig.Builder stratego = CfgStrategoConfig.builder();

    public CompileMetaLanguageSourcesInput.Builder compileLanguage = CompileMetaLanguageSourcesInput.builder();


    public CfgSdf3Config.Builder withSdf3() {
        sdf3Enabled = true;
        return sdf3;
    }

    public CfgEsvConfig.Builder withEsv() {
        esvEnabled = true;
        return esv;
    }

    public CfgStatixConfig.Builder withStatix() {
        statixEnabled = true;
        return statix;
    }

    public CfgDynamixConfig.Builder withDynamix() {
        dynamixEnabled = true;
        return dynamix;
    }

    public CfgStrategoConfig.Builder withStratego() {
        strategoEnabled = true;
        return stratego;
    }


    public CompileMetaLanguageSourcesInput build(Properties persistentProperties, Shared shared, CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared) {
        final @Nullable CfgSdf3Config sdf3 = buildSdf3(compileMetaLanguageSourcesShared);
        if(sdf3 != null) compileLanguage.sdf3(sdf3);

        final @Nullable CfgEsvConfig esv = buildEsv(compileMetaLanguageSourcesShared);
        if(esv != null) compileLanguage.esv(esv);

        final @Nullable CfgStatixConfig statix = buildStatix(compileMetaLanguageSourcesShared);
        if(statix != null) compileLanguage.statix(statix);

        final @Nullable CfgDynamixConfig dynamix = buildDynamix(compileMetaLanguageSourcesShared);
        if(dynamix != null) compileLanguage.dynamix(dynamix);

        final @Nullable CfgStrategoConfig stratego = buildStratego(persistentProperties, shared, compileMetaLanguageSourcesShared);
        if(stratego != null) compileLanguage.stratego(stratego);

        return compileLanguage
            .shared(shared)
            .compileLanguageShared(compileMetaLanguageSourcesShared)
            .build();
    }


    private @Nullable CfgSdf3Config buildSdf3(
        CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared
    ) {
        if(!sdf3Enabled) return null;
        return sdf3
            .compileLanguageShared(compileMetaLanguageSourcesShared)
            .build();
    }

    private @Nullable CfgEsvConfig buildEsv(
        CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared
    ) {
        if(!esvEnabled) return null;
        return esv
            .compileLanguageShared(compileMetaLanguageSourcesShared)
            .build();
    }

    private @Nullable CfgStatixConfig buildStatix(
        CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared
    ) {
        if(!statixEnabled) return null;
        return statix
            .compileLanguageShared(compileMetaLanguageSourcesShared)
            .build();
    }

    private @Nullable CfgDynamixConfig buildDynamix(
        CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared
    ) {
        if(!dynamixEnabled) return null;
        return dynamix
            .compileLanguageShared(compileMetaLanguageSourcesShared)
            .build();
    }

    private @Nullable CfgStrategoConfig buildStratego(
        Properties persistentProperties,
        Shared shared,
        CompileMetaLanguageSourcesShared compileMetaLanguageSourcesShared
    ) {
        if(!strategoEnabled) return null;
        return stratego
            .withPersistentProperties(persistentProperties)
            .shared(shared)
            .compileLanguageShared(compileMetaLanguageSourcesShared)
            .build();
    }
}
