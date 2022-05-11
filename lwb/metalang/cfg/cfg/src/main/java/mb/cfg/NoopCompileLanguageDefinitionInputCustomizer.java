package mb.cfg;

import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.util.Shared;

import java.util.Optional;

/**
 * {@link CompileLanguageDefinitionInputCustomizer} implementation that does nothing.
 */
public class NoopCompileLanguageDefinitionInputCustomizer implements CompileLanguageDefinitionInputCustomizer {
    @Override public void customize(Shared.Builder builder) {

    }

    @Override public void customize(LanguageProject.Builder builder) {

    }

    @Override public void customize(LanguageProjectCompilerInputBuilder baseBuilder) {

    }

    @Override public void customize(CompileMetaLanguageSourcesShared.Builder builder) {

    }

    @Override public void customize(CompileMetaLanguageSourcesInputBuilder languageCompilerInputBuilder) {

    }

    @Override public void customize(AdapterProject.Builder builder) {

    }

    @Override public void customize(AdapterProjectCompilerInputBuilder adapterBuilder) {

    }

    @Override public boolean customize(EclipseProjectCompiler.Input.Builder builder) {
        return true;
    }

    @Override public Optional<EclipseProjectCompiler.Input.Builder> getDefaultEclipseProjectInput() {
        return Optional.empty();
    }

    @Override public void customize(CompileLanguageDefinitionInput.Builder builder) {

    }
}
