package mb.spoofax.dynamicloading;

import mb.common.option.Option;
import mb.common.style.Color;
import mb.common.style.Style;
import mb.common.style.Styling;
import mb.common.style.TokenStyle;
import mb.common.util.Properties;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.task.archive.UnarchiveCommon;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DynamicLoadTest {
    private final DynamicLoader dynamicLoader = new DynamicLoader();

    @Test void testDynamicLoadCharsLanguage(@TempDir Path temporaryDirectoryPath) throws Exception {
        // Copy language specification sources to the temporary directory.
        final FSResource temporaryDirectory = new FSResource(temporaryDirectoryPath);
        copyResourcesToTemporaryDirectory("mb/spoofax/dynamicloading/chars", temporaryDirectory);

        // Create the compiler inputs.
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
        final LanguageProjectCompiler.Input languageProjectInput = languageProjectInputBuilder.build(shared, languageProject);

        adapterProjectCompilerInputBuilder.withParser();
        adapterProjectCompilerInputBuilder.withStyler();
        final AdapterProjectCompiler.Input adapterProjectInput = adapterProjectCompilerInputBuilder.build(languageProjectInput, Option.ofNone(), adapterProject);

        final CompileToJavaClassFiles.Input input = CompileToJavaClassFiles.Input.builder()
            .languageProjectInput(languageProjectInput)
            .spoofax3LanguageProjectInput(spoofax3LanguageProjectInput)
            .adapterProjectInput(adapterProjectInput)
            .build();

        // Create the dynamic language component.
        try(final DynamicLanguageComponent languageComponent = dynamicLoader.createDynamicLanguageComponent(input)) {
            final FSResource file = temporaryDirectory.appendSegment("test.chars");
            file.ensureFileExists().writeString("abcdefg");
            try(final MixedSession session = languageComponent.getPie().newSession()) {
                final Option<Styling> result = session.require(languageComponent.getLanguageInstance().createStyleTask(file.getPath()));
                assertTrue(result.isSome());
                final Styling styling = result.unwrap();
                final ArrayList<TokenStyle> stylingPerToken = styling.getStylePerToken();
                assertEquals(1, stylingPerToken.size());
                final Style style = stylingPerToken.get(0).getStyle();
                assertNotNull(style.getColor());
                assertNull(style.getBackgroundColor());
                assertEquals(new Color(0, 0, 150), style.getColor());
                assertTrue(style.isBold());
                assertFalse(style.isItalic());
                assertFalse(style.isUnderscore());
                assertFalse(style.isStrikeout());
            } catch(ExecException e) {
                if(e.getCause() instanceof CompileToJavaClassFiles.CompileException) {
                    final CompileToJavaClassFiles.CompileException compilerException = (CompileToJavaClassFiles.CompileException)e.getCause();
                    System.err.println(compilerException.getMessage());
                    compilerException.getSubMessage().ifPresent(System.err::println);
                    compilerException.getSubMessages().ifPresent(System.err::println);
                }
                throw e;
            }
        }
    }

    void copyResourcesToTemporaryDirectory(String sourceFilesPath, HierarchicalResource temporaryDirectory) throws IOException {
        final ClassLoaderResource sourceFilesDirectory = dynamicLoader.classLoaderResourceRegistry.getResource(sourceFilesPath);
        final ClassLoaderResourceLocations locations = sourceFilesDirectory.getLocations();
        for(FSResource directory : locations.directories) {
            directory.copyRecursivelyTo(temporaryDirectory);
        }
        for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
            UnarchiveCommon.unarchiveJar(jarFileWithPath.file, temporaryDirectory, false, false);
        }
    }
}
