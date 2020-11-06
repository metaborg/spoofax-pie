package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class AdapterProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            final AdapterProjectCompiler.Input input = compileLanguageAndAdapterProject(session, inputs);
            fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
                s.asserts(input.packageInfo(), FileAssertion::assertNotExists);
                s.assertPublicJavaInterface(input.baseComponent(), "TigerComponent");
                s.assertPublicJavaClass(input.baseModule(), "TigerModule");
                s.assertPublicJavaClass(input.baseInstance(), "TigerInstance");
                s.assertPublicJavaClass(input.baseCheckTaskDef(), "TigerCheck");
            });
        }
    }

    @Test void testAdapterProjectAsSeparatePRoject(@TempDir Path temporaryDirectoryPath) throws Exception {
        final TigerInputs inputs = defaultInputsWithSeparateAdapterProject();

        try(MixedSession session = pie.newSession()) {
            final AdapterProjectCompiler.Input input = compileLanguageAndAdapterProject(session, inputs);
            fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
                s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
                s.assertPublicJavaInterface(input.baseComponent(), "TigerComponent");
                s.assertPublicJavaClass(input.baseModule(), "TigerModule");
                s.assertPublicJavaClass(input.baseInstance(), "TigerInstance");
                s.assertPublicJavaClass(input.baseCheckTaskDef(), "TigerCheck");
            });
        }
    }
}
