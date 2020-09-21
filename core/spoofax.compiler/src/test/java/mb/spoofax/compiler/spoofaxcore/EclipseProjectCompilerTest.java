package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class EclipseProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            compileLanguageAndAdapterProject(session, inputs);
            session.require(component.getEclipseExternaldepsProjectCompiler().createTask(inputs.eclipseExternaldepsProjectInput().build()));

            final EclipseProjectCompiler.Input input = inputs.eclipseProjectInput().build();
            session.require(component.getEclipseProjectCompiler().createTask(input));
            fileAssertions.asserts(input.pluginXmlFile(), (s) -> s.assertAll("plugin.xml", "<plugin>"));
            fileAssertions.asserts(input.manifestMfFile(), (s) -> s.assertAll("MANIFEST.MF", "Export-Package"));
            fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
                s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
                s.assertPublicJavaClass(input.genPlugin(), "TigerPlugin");
                s.assertPublicJavaInterface(input.genEclipseComponent(), "TigerEclipseComponent");
                s.assertPublicJavaClass(input.genEclipseModule(), "TigerEclipseModule");
                s.assertPublicJavaClass(input.genEclipseIdentifiers(), "TigerEclipseIdentifiers");
                s.assertPublicJavaClass(input.genEditor(), "TigerEditor");
                s.assertPublicJavaClass(input.genEditorTracker(), "TigerEditorTracker");
                s.assertPublicJavaClass(input.genNature(), "TigerNature");
                s.assertPublicJavaClass(input.genAddNatureHandler(), "TigerAddNatureHandler");
                s.assertPublicJavaClass(input.genRemoveNatureHandler(), "TigerRemoveNatureHandler");
                s.assertPublicJavaClass(input.genProjectBuilder(), "TigerProjectBuilder");
                s.assertPublicJavaClass(input.genMainMenu(), "TigerMainMenu");
                s.assertPublicJavaClass(input.genEditorContextMenu(), "TigerEditorContextMenu");
                s.assertPublicJavaClass(input.genResourceContextMenu(), "TigerResourceContextMenu");
                s.assertPublicJavaClass(input.genRunCommandHandler(), "TigerRunCommandHandler");
                s.assertPublicJavaClass(input.genObserveHandler(), "TigerObserveHandler");
                s.assertPublicJavaClass(input.genUnobserveHandler(), "TigerUnobserveHandler");
            });
        }
    }
}
