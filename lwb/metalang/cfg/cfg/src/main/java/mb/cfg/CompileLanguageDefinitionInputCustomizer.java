package mb.cfg;

import mb.cfg.convert.CfgAstToObject;
import mb.cfg.task.CfgToObject;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.util.Shared;

import java.util.Optional;

/**
 * Interface for customizing {@link CompileLanguageDefinitionInput} objects, used in {@link CfgAstToObject} as part of
 * {@link CfgToObject}. An implementation is provided by {@link CfgCustomizerModule} which is then injected into the
 * relevant objects. Pass a {@link CfgCustomizerModule} object when building the {@link CfgComponent} via
 * {@link DaggerCfgComponent.Builder#cfgCustomizerModule(CfgCustomizerModule)}.
 */
public interface CompileLanguageDefinitionInputCustomizer {
    void customize(Shared.Builder builder);


    void customize(LanguageProject.Builder builder);

    void customize(LanguageProjectCompilerInputBuilder baseBuilder);


    void customize(CompileMetaLanguageSourcesShared.Builder builder);

    void customize(CompileMetaLanguageSourcesInputBuilder languageCompilerInputBuilder);


    void customize(AdapterProject.Builder builder);

    void customize(AdapterProjectCompilerInputBuilder adapterBuilder);


    boolean customize(EclipseProjectCompiler.Input.Builder builder);

    Optional<EclipseProjectCompiler.Input.Builder> getDefaultEclipseProjectInput();


    void customize(CompileLanguageDefinitionInput.Builder builder);
}
