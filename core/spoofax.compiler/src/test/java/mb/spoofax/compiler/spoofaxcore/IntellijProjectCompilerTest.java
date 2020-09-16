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
            fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
                s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
                s.assertPublicJavaClass(input.genPlugin(), "TigerPlugin");
                s.assertPublicJavaInterface(input.genComponent(), "TigerIntellijComponent");
                s.assertPublicJavaClass(input.genModule(), "TigerIntellijModule");
                s.assertPublicJavaClass(input.genPlugin(), "TigerPlugin");
                s.assertPublicJavaClass(input.genLoader(), "TigerLoader");
                s.assertPublicJavaClass(input.genLanguage(), "TigerLanguage");
                s.assertPublicJavaClass(input.genFileType(), "TigerFileType");
                s.assertPublicJavaClass(input.genFileElementType(), "TigerFileElementType");
                s.assertPublicJavaClass(input.genFileTypeFactory(), "TigerFileTypeFactory");
                s.assertPublicJavaClass(input.genSyntaxHighlighterFactory(), "TigerSyntaxHighlighterFactory");
                s.assertPublicJavaClass(input.genParserDefinition(), "TigerParserDefinition");
            });
        }
    }
}
