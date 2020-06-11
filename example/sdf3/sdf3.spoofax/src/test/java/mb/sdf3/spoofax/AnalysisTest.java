package mb.sdf3.spoofax;

import mb.common.message.KeyedMessages;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.ValueSupplier;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.text.TextResource;
import mb.sdf3.Sdf3ClassloaderResources;
import mb.statix.multilang.AnalysisContext;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.DaggerMultiLangComponent;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.multilang.spec.SpecUtils;
import mb.statix.multilang.tasks.SmlAnalyzeProject;
import mb.statix.multilang.tasks.SmlBuildMessages;
import mb.statix.multilang.tasks.SmlInstantiateGlobalScope;
import mb.statix.multilang.tasks.SmlPartialSolveFile;
import mb.statix.multilang.tasks.SmlPartialSolveProject;
import org.junit.jupiter.api.Test;
import org.metaborg.util.log.Level;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnalysisTest extends TestBase {

    protected final ClassLoaderResourceRegistry statixRegistry = Sdf3ClassloaderResources.createClassLoaderResourceRegistry();
    protected ITermFactory termFactory = new TermFactory();
    private final ResourcePath projectPath = new FSPath(".");
    private final MultiLangComponent multilangComponent = DaggerMultiLangComponent.builder()
        .platformComponent(platformComponent)
        .build();
    private final AnalysisContextService analysisContextService = multilangComponent.getAnalysisContextService();

    private final SmlInstantiateGlobalScope instantiateGlobalScope = new SmlInstantiateGlobalScope();
    private final SmlPartialSolveProject partialSolveProject = new SmlPartialSolveProject();
    private final SmlPartialSolveFile partialSolveFile = new SmlPartialSolveFile(languageComponent.getStrategoRuntimeBuilder().
        build().getTermFactory());
    private final SmlAnalyzeProject analyzeProject = new SmlAnalyzeProject(instantiateGlobalScope, partialSolveProject, partialSolveFile);
    private final SmlBuildMessages buildMessages = new SmlBuildMessages(analyzeProject);

    private final Level logLevel = Level.Warn;

    @Test void testSingleError() throws IOException, ExecException {
        final HashSet<ResourceKey> resources = new HashSet<>();
        final TextResource resource = createTextResource("module a syntax A = B", "a.sdf3");
        resources.add(resource.getKey());

        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = context.createPieForContext().newSession()) {
            KeyedMessages messages = session.require(buildMessages
                .createTask(new SmlBuildMessages.Input(projectPath, context, logLevel)));
            assertTrue(messages.containsError());
            assertEquals(1, messages.count());
            assertEquals(1, messages.getAllMessages().get(resource.getKey()).size());
        }
    }

    @Test void testSingleSuccess() throws IOException, ExecException {
        final HashSet<ResourceKey> resources = new HashSet<>();
        final TextResource resource1 = createTextResource("module a", "a.sdf3");
        resources.add(resource1.getKey());

        // Loading spec
        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = context.createPieForContext().newSession()) {
            KeyedMessages messages = session.require(buildMessages
                .createTask(new SmlBuildMessages.Input(projectPath, context, logLevel)));
            assertEquals(0, messages.count());
        }
    }

    @Test void testMultipleErrors() throws IOException, ExecException {
        final HashSet<ResourceKey> resources = new HashSet<>();
        final TextResource resource1 = createTextResource("module a syntax B = A", "a.sdf3");
        final TextResource resource2 = createTextResource("module b syntax C = A B", "b.sdf3");
        resources.add(resource1.getKey());
        resources.add(resource2.getKey());

        // Loading spec
        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = context.createPieForContext().newSession()) {
            KeyedMessages messages = session.require(buildMessages
                .createTask(new SmlBuildMessages.Input(projectPath, context, logLevel)));
            assertTrue(messages.containsError());
            assertEquals(4, messages.count());
            assertTrue(messages.containsWarning());
            assertEquals(1, messages.getAllMessages().get(resource1.getKey()).size());
            assertEquals(3, messages.getAllMessages().get(resource2.getKey()).size());
        }
    }

    @Test void testMultipleSuccess() throws IOException, ExecException {
        final HashSet<ResourceKey> resources = new HashSet<>();
        final TextResource resource1 = createTextResource("module a", "a.sdf3");
        final TextResource resource2 = createTextResource("module b", "b.sdf3");
        resources.add(resource1.getKey());
        resources.add(resource2.getKey());

        // Loading spec
        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = context.createPieForContext().newSession()) {
            KeyedMessages messages = session.require(buildMessages
                .createTask(new SmlBuildMessages.Input(projectPath, context, logLevel)));
            assertFalse(messages.containsError());
            assertEquals(0, messages.count());
        }
    }

    @Test void testMutualResolve() throws IOException, ExecException {
        final HashSet<ResourceKey> resources = new HashSet<>();
        final TextResource resource1 = createTextResource("module a syntax A = \"\"", "a.sdf3");
        final TextResource resource2 = createTextResource("module b imports a syntax B = A", "b.sdf3");
        final TextResource resource3 = createTextResource("module c imports a b syntax C = A syntax C = B", "c.sdf3");
        resources.add(resource1.getKey());
        resources.add(resource2.getKey());
        resources.add(resource3.getKey());

        // Loading spec
        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = context.createPieForContext().newSession()) {
            KeyedMessages messages = session.require(buildMessages
                .createTask(new SmlBuildMessages.Input(projectPath, context, logLevel)));
            assertFalse(messages.containsError());
            assertEquals(0, messages.count());
        }
    }

    private AnalysisContext createAnalysisContext(HashSet<ResourceKey> resources) throws IOException {
        ResourceKeyString id = ResourceKeyString.of("mb/sdf3/src-gen/statix");
        ClassLoaderResource statixSpec = statixRegistry.getResource(id);
        SpecBuilder spec = SpecUtils.loadSpec(statixSpec, "statix/statics", termFactory);

        LanguageMetadata languageMetadata = LanguageMetadata.builder()
            .languageId(new LanguageId("sdf3"))
            .statixSpec(spec)
            .fileConstraint("statix/statics!moduleOK")
            .projectConstraint("statix/statics!projectOK")
            .resourcesSupplier((c, p) -> resources)
            .astFunction(languageComponent.getPreAnalysisTransform().createFunction())
            .addTaskDefs(instantiateGlobalScope, partialSolveProject, partialSolveFile, analyzeProject, buildMessages,
                languageComponent.getPreStatix(), languageComponent.getPreAnalysisTransform(),
                languageComponent.getPostStatix(), parse)
            .addResourceRegistries()
            // TODO: remove ValueSupplier somehow
            .postTransform(languageComponent.getPostStatix().createFunction().mapInput(ValueSupplier::new))
            .build();

        return analysisContextService.createContext("AnalysisTest", languageMetadata);
    }
}
