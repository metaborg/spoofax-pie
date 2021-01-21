package mb.spoofax.dynamicloading;

import mb.common.option.Option;
import mb.common.style.Color;
import mb.common.style.Style;
import mb.common.style.Styling;
import mb.common.style.TokenStyle;
import mb.common.util.Properties;
import mb.log.api.LoggerFactory;
import mb.log.stream.StreamLoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.PieBuilder;
import mb.pie.api.Tracer;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.store.SerializingStore;
import mb.pie.runtime.tracer.LoggingTracer;
import mb.pie.task.archive.UnarchiveCommon;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
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
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceRegistriesModule;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class DynamicLoadTest {
    final ClassLoaderResourceRegistry classLoaderResourceRegistry = new ClassLoaderResourceRegistry("spoofax3.standalone", DynamicLoadTest.class.getClassLoader());

    @Test void testDynamicLoadCharsLanguage(@TempDir Path temporaryDirectoryPath) throws Exception {
        // Copy language specification sources to the temporary directory.
        final FSResource temporaryDirectory = new FSResource(temporaryDirectoryPath);
        copyResourcesToTemporaryDirectory("mb/spoofax/dynamicloading/chars", temporaryDirectory);

        // Create platform component.
        final WritableResource pieStore = temporaryDirectory.appendRelativePath("pie.store");
        // TODO: use serializing store to serialize/deserialize store after language reload
        final PieBuilder.StoreFactory storeFactory = (serde, __, ___) -> new SerializingStore<>(serde, pieStore, InMemoryStore::new, InMemoryStore.class, false);
        final Function<LoggerFactory, Tracer> tracerFactory = LoggingTracer::new;
        final PlatformComponent platformComponent = DaggerPlatformComponent.builder()
            .loggerFactoryModule(new LoggerFactoryModule(StreamLoggerFactory.stdOutVeryVerbose()))
            .resourceRegistriesModule(new ResourceRegistriesModule(classLoaderResourceRegistry))
            .platformPieModule(new PlatformPieModule(PieBuilderImpl::new, storeFactory, tracerFactory))
            .build();
        final ResourceService resourceService = platformComponent.getResourceService();

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

        // Write test file.
        final FSResource file = temporaryDirectory.appendSegment("test.chars");
        file.ensureFileExists().writeString("abcdefg");

        try(final DynamicLoader dynamicLoader = new DynamicLoader(platformComponent)) {
            @Nullable DynamicLanguage dynamicLanguageCached1;
            {
                // Dynamically load language.
                final DynamicLanguage dynamicLanguage = dynamicLoader.load("chars", input);
                dynamicLanguageCached1 = dynamicLanguage;
                final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
                // Style test file with dynamically loaded language.
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
                    logAndRethrow(e);
                }
            }

            // Dynamic language has not yet been closed.
            assertNotNull(dynamicLanguageCached1.getClassLoader());
            assertNotNull(dynamicLanguageCached1.getLanguageComponent());
            assertFalse(dynamicLanguageCached1.isClosed());

            // Change the language specification.
            final ResourcePath esvMainFilePath = input.spoofax3LanguageProjectInput().styler().get().esvMainFile();
            final WritableResource esvMainFile = resourceService.getWritableResource(esvMainFilePath);
            final String esvMainString = esvMainFile.readString().replace("0 0 150 bold", "255 255 0 italic");
            esvMainFile.writeString(esvMainString);
            final HashSet<ResourceKey> changedResources = new HashSet<>();
            changedResources.add(esvMainFilePath);
            dynamicLoader.updateAffectedBy(changedResources);

            @Nullable DynamicLanguage dynamicLanguageCached2;
            {
                // Dynamically load language again.
                final DynamicLanguage dynamicLanguage = dynamicLoader.load("chars", input);
                dynamicLanguageCached2 = dynamicLanguage;
                final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
                // Style test file with dynamically loaded language again
                try(final MixedSession session = languageComponent.getPie().newSession()) {
                    final Option<Styling> result = session.require(languageComponent.getLanguageInstance().createStyleTask(file.getPath()));
                    assertTrue(result.isSome());
                    final Styling styling = result.unwrap();
                    final ArrayList<TokenStyle> stylingPerToken = styling.getStylePerToken();
                    assertEquals(1, stylingPerToken.size());
                    final Style style = stylingPerToken.get(0).getStyle();
                    assertNotNull(style.getColor());
                    assertNull(style.getBackgroundColor());
                    assertEquals(new Color(255, 255, 0), style.getColor());
                    assertFalse(style.isBold());
                    assertTrue(style.isItalic());
                    assertFalse(style.isUnderscore());
                    assertFalse(style.isStrikeout());
                } catch(ExecException e) {
                    logAndRethrow(e);
                }
            }

            // New dynamic language has not yet been closed.
            assertNotNull(dynamicLanguageCached2.getClassLoader());
            assertNotNull(dynamicLanguageCached2.getLanguageComponent());
            assertFalse(dynamicLanguageCached2.isClosed());
            // Previous dynamic language should be closed.
            assertThrows(IllegalStateException.class, dynamicLanguageCached1::getClassLoader);
            assertThrows(IllegalStateException.class, dynamicLanguageCached1::getLanguageComponent);
            assertTrue(dynamicLanguageCached1.isClosed());
            dynamicLanguageCached1 = null;

            // Unload the new dynamic language.
            dynamicLoader.unload("chars");
            // New dynamic language should be closed.
            assertThrows(IllegalStateException.class, dynamicLanguageCached2::getClassLoader);
            assertThrows(IllegalStateException.class, dynamicLanguageCached2::getLanguageComponent);
            assertTrue(dynamicLanguageCached2.isClosed());
            dynamicLanguageCached2 = null;

            // Cleanup cache.
            dynamicLoader.deleteCacheForUnloadedLanguages();
        }
    }

    void copyResourcesToTemporaryDirectory(String sourceFilesPath, HierarchicalResource temporaryDirectory) throws IOException {
        final ClassLoaderResource sourceFilesDirectory = classLoaderResourceRegistry.getResource(sourceFilesPath);
        final ClassLoaderResourceLocations locations = sourceFilesDirectory.getLocations();
        for(FSResource directory : locations.directories) {
            directory.copyRecursivelyTo(temporaryDirectory);
        }
        for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
            UnarchiveCommon.unarchiveJar(jarFileWithPath.file, temporaryDirectory, false, false);
        }
    }

    void logAndRethrow(ExecException e) throws ExecException {
        if(e.getCause() instanceof CompileToJavaClassFiles.CompileException) {
            final CompileToJavaClassFiles.CompileException compilerException = (CompileToJavaClassFiles.CompileException)e.getCause();
            System.err.println(compilerException.getMessage());
            compilerException.getSubMessage().ifPresent(System.err::println);
            compilerException.getSubMessages().ifPresent(System.err::println);
        }
        throw e;
    }
}
