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
            final EclipseProjectCompiler.Input input = inputs.eclipseProjectInput().build();
            session.require(component.getEclipseProjectCompiler().createTask(input));
            fileAssertions.asserts(input.pluginXmlFile(), (s) -> s.assertAll("plugin.xml", "<plugin>"));
            fileAssertions.asserts(input.manifestMfFile(), (s) -> s.assertAll("MANIFEST.MF", "Bundle-ManifestVersion"));
            fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
                s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
                s.assertPublicJavaClass(input.basePlugin(), "TigerPlugin");
                s.assertPublicJavaInterface(input.baseEclipseComponent(), "TigerEclipseComponent");
                s.assertPublicJavaClass(input.baseEclipseModule(), "TigerEclipseModule");
                s.assertPublicJavaClass(input.baseEclipseIdentifiers(), "TigerEclipseIdentifiers");
                s.assertPublicJavaClass(input.baseEditor(), "TigerEditor");
                s.assertPublicJavaClass(input.baseEditorTracker(), "TigerEditorTracker");
                s.assertPublicJavaClass(input.baseNature(), "TigerNature");
                s.assertPublicJavaClass(input.baseAddNatureHandler(), "TigerAddNatureHandler");
                s.assertPublicJavaClass(input.baseRemoveNatureHandler(), "TigerRemoveNatureHandler");
                s.assertPublicJavaClass(input.baseProjectBuilder(), "TigerProjectBuilder");
                s.assertPublicJavaClass(input.baseMainMenu(), "TigerMainMenu");
                s.assertPublicJavaClass(input.baseEditorContextMenu(), "TigerEditorContextMenu");
                s.assertPublicJavaClass(input.baseResourceContextMenu(), "TigerResourceContextMenu");
                s.assertPublicJavaClass(input.baseRunCommandHandler(), "TigerRunCommandHandler");
                s.assertPublicJavaClass(input.baseObserveHandler(), "TigerObserveHandler");
                s.assertPublicJavaClass(input.baseUnobserveHandler(), "TigerUnobserveHandler");
            });
        }
    }
}
