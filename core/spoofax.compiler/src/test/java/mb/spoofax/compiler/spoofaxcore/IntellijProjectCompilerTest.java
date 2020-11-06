package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.platform.IntellijProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class IntellijProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            compileLanguageAndAdapterProject(session, inputs);

            final IntellijProjectCompiler.Input input = inputs.intellijProjectInput().build();
            session.require(component.getIntellijProjectCompiler().createTask(input));
            fileAssertions.asserts(input.pluginXmlFile(), (a) -> a.assertAll("plugin.xml", "<idea-plugin>"));
            fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
                s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
                s.assertPublicJavaClass(input.basePlugin(), "TigerPlugin");
                s.assertPublicJavaInterface(input.baseComponent(), "TigerIntellijComponent");
                s.assertPublicJavaClass(input.baseModule(), "TigerIntellijModule");
                s.assertPublicJavaClass(input.basePlugin(), "TigerPlugin");
                s.assertPublicJavaClass(input.baseLoader(), "TigerLoader");
                s.assertPublicJavaClass(input.baseLanguage(), "TigerLanguage");
                s.assertPublicJavaClass(input.baseFileType(), "TigerFileType");
                s.assertPublicJavaClass(input.baseFileElementType(), "TigerFileElementType");
                s.assertPublicJavaClass(input.baseFileTypeFactory(), "TigerFileTypeFactory");
                s.assertPublicJavaClass(input.baseSyntaxHighlighterFactory(), "TigerSyntaxHighlighterFactory");
                s.assertPublicJavaClass(input.baseParserDefinition(), "TigerParserDefinition");
            });
        }
    }
}
