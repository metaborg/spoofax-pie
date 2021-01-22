package mb.spoofax.dynamicloading;

import mb.common.option.Option;
import mb.common.util.Properties;
import mb.pie.api.ExecException;
import mb.resource.ResourceKey;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProject;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;
import mb.spoofax.compiler.util.Shared;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

class CharsTestBase extends TestBase {
    LanguageProjectCompiler.Input languageProjectInput;
    AdapterProjectCompiler.Input adapterProjectInput;
    CompileToJavaClassFiles.Input input;
    HierarchicalResource charsFile;

    void setup(Path temporaryDirectoryPath) throws IOException {
        super.setup(temporaryDirectoryPath);

        copyResourcesToTemporaryDirectory("mb/spoofax/dynamicloading/chars");

        final String packageId = "mb.chars";
        final Shared shared = Shared.builder()
            .name("Chars")
            .defaultPackageId(packageId)
            .defaultClassPrefix("Chars")
            .build();

        final LanguageProject languageProject = LanguageProject.builder().withDefaults(temporaryDirectory.getPath(), shared).build();
        final Spoofax3LanguageProject spoofax3LanguageProject = Spoofax3LanguageProject.builder().languageProject(languageProject).build();
        final AdapterProject adapterProject = AdapterProject.builder().withDefaults(temporaryDirectory.getPath(), shared).build();

        final LanguageProjectCompilerInputBuilder languageProjectInputBuilder = new LanguageProjectCompilerInputBuilder();
        final Spoofax3LanguageProjectCompilerInputBuilder spoofax3LanguageProjectInputBuilder = new Spoofax3LanguageProjectCompilerInputBuilder();
        final AdapterProjectCompilerInputBuilder adapterProjectCompilerInputBuilder = new AdapterProjectCompilerInputBuilder();

        spoofax3LanguageProjectInputBuilder.withParser();
        spoofax3LanguageProjectInputBuilder.withStyler();
        final Spoofax3LanguageProjectCompiler.Input spoofax3LanguageProjectInput = spoofax3LanguageProjectInputBuilder.build(new Properties(), shared, spoofax3LanguageProject);
        spoofax3LanguageProjectInput.syncTo(languageProjectInputBuilder);

        languageProjectInputBuilder.withParser().startSymbol("Start");
        languageProjectInputBuilder.withStyler();
        this.languageProjectInput = languageProjectInputBuilder.build(shared, languageProject);

        adapterProjectCompilerInputBuilder.withParser();
        adapterProjectCompilerInputBuilder.withStyler();
        this.adapterProjectInput = adapterProjectCompilerInputBuilder.build(languageProjectInput, Option.ofNone(), adapterProject);

        this.input = CompileToJavaClassFiles.Input.builder()
            .languageProjectInput(languageProjectInput)
            .spoofax3LanguageProjectInput(spoofax3LanguageProjectInput)
            .adapterProjectInput(adapterProjectInput)
            .build();

        this.charsFile = temporaryDirectory.appendSegment("test.chars").ensureFileExists();
        charsFile.writeString("abcdefg");
    }

    void teardown() throws Exception {
        charsFile = null;
        input = null;
        adapterProjectInput = null;
        languageProjectInput = null;
        super.teardown();
    }


    Set<ResourceKey> modifyStyler() throws IOException, ExecException, InterruptedException {
        final ResourcePath esvMainFilePath = input.spoofax3LanguageProjectInput().styler().get().esvMainFile();
        final WritableResource esvMainFile = resourceService.getWritableResource(esvMainFilePath);
        final String esvMainString = esvMainFile.readString().replace("0 0 150 bold", "255 255 0 italic");
        esvMainFile.writeString(esvMainString);
        return dynamicLoader.updateAffectedBy(esvMainFilePath);
    }

    Set<ResourceKey> modifyParser() throws IOException, ExecException, InterruptedException {
        final ResourcePath sdf3MainFilePath = input.spoofax3LanguageProjectInput().parser().get().sdf3MainFile();
        final WritableResource sdf3MainFile = resourceService.getWritableResource(sdf3MainFilePath);
        final String sdf3MainString = sdf3MainFile.readString().replace("\\ ", "\\ \\t");
        sdf3MainFile.writeString(sdf3MainString);
        return dynamicLoader.updateAffectedBy(sdf3MainFilePath);
    }
}
