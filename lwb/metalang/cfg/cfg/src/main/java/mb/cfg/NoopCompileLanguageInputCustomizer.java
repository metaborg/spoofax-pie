package mb.cfg;

import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.util.Shared;

/**
 * {@link CompileLanguageInputCustomizer} implementation that does nothing.
 */
public class NoopCompileLanguageInputCustomizer implements CompileLanguageInputCustomizer {
    @Override public void customize(Shared.Builder builder) {

    }

    @Override public void customize(LanguageProject.Builder builder) {

    }

    @Override public void customize(LanguageProjectCompilerInputBuilder baseBuilder) {

    }

    @Override public void customize(CompileLanguageSpecificationShared.Builder builder) {

    }

    @Override public void customize(CompileLanguageSpecificationInputBuilder languageCompilerInputBuilder) {

    }

    @Override public void customize(AdapterProject.Builder builder) {

    }

    @Override public void customize(AdapterProjectCompilerInputBuilder adapterBuilder) {

    }

    @Override public void customize(EclipseProjectCompiler.Input.Builder builder) {

    }

    @Override public void customize(CompileLanguageInput.Builder builder) {

    }
}
