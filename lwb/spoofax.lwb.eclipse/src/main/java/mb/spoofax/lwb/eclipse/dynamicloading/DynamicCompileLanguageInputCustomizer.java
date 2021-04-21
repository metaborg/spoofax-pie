package mb.spoofax.lwb.eclipse.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.cfg.CompileLanguageInputCustomizer;
import mb.cfg.CompileLanguageSpecificationInputBuilder;
import mb.cfg.CompileLanguageSpecificationShared;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.util.Shared;

public class DynamicCompileLanguageInputCustomizer implements CompileLanguageInputCustomizer {
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
        // Override several identifiers to `spoofax.lwb.eclipse.dynamicloading.*` versions, as their own implementations
        // are not available when dynamically loaded.
        builder.baseMarkerId("spoofax.lwb.eclipse.dynamicloading.marker");
        builder.infoMarkerId("spoofax.lwb.eclipse.dynamicloading.marker.info");
        builder.warningMarkerId("spoofax.lwb.eclipse.dynamicloading.marker.warning");
        builder.errorMarkerId("spoofax.lwb.eclipse.dynamicloading.marker.error");
        builder.contextId("spoofax.lwb.eclipse.dynamicloading.context");
        builder.runCommandId("spoofax.lwb.eclipse.dynamicloading.runcommand");
        builder.natureRelativeId(DynamicNature.relativeId);
        builder.natureId(DynamicNature.id);
        builder.addNatureCommandId("spoofax.lwb.eclipse.dynamicloading.nature.add");
        builder.removeNatureCommandId("spoofax.lwb.eclipse.dynamicloading.nature.remove");
        builder.projectBuilderRelativeId(DynamicProjectBuilder.relativeId);
        builder.projectBuilderId(DynamicProjectBuilder.id);
    }

    @Override public void customize(CompileLanguageInput.Builder builder) {

    }
}
