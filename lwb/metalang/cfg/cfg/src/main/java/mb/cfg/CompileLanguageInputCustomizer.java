package mb.cfg;

import mb.cfg.task.AstToObject;
import mb.cfg.task.CfgToObject;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.util.Shared;

/**
 * Interface for customizing {@link CompileLanguageInput} objects, used in {@link AstToObject} as part of {@link
 * CfgToObject}. An implementation is provided by {@link CfgCustomizerModule} which is then injected into the relevant
 * objects. Pass a {@link CfgCustomizerModule} object when building the {@link CfgComponent} via {@link
 * DaggerCfgComponent.Builder#cfgCustomizerModule(CfgCustomizerModule)}.
 */
public interface CompileLanguageInputCustomizer {
    void customize(Shared.Builder builder);


    void customize(LanguageProject.Builder builder);

    void customize(LanguageProjectCompilerInputBuilder baseBuilder);


    void customize(CompileLanguageSpecificationShared.Builder builder);

    void customize(CompileLanguageSpecificationInputBuilder languageCompilerInputBuilder);


    void customize(AdapterProject.Builder builder);

    void customize(AdapterProjectCompilerInputBuilder adapterBuilder);


    void customize(EclipseProjectCompiler.Input.Builder builder);


    void customize(CompileLanguageInput.Builder builder);
}
