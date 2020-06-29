package mb.sdf3.spoofax;

import com.google.common.collect.Lists;
import mb.common.message.KeyedMessages;
import mb.pie.api.ExecException;
import mb.pie.api.Function;
import mb.pie.api.MixedSession;
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
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.DaggerMultiLangComponent;
import mb.statix.multilang.ImmutableLanguageMetadata;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.LanguageMetadata;
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.spec.SpecBuilder;
import mb.statix.multilang.spec.SpecUtils;
import mb.statix.multilang.pie.SmlAnalyzeProject;
import mb.statix.multilang.pie.SmlBuildMessages;
import mb.statix.multilang.pie.SmlInstantiateGlobalScope;
import mb.statix.multilang.pie.SmlPartialSolveFile;
import mb.statix.multilang.pie.SmlPartialSolveProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metaborg.util.log.Level;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnalysisTest extends TestBase {

    protected final ClassLoaderResourceRegistry statixRegistry = Sdf3ClassloaderResources.createClassLoaderResourceRegistry();
    protected final ITermFactory termFactory = new TermFactory();
    private final ResourcePath projectPath = new FSPath(".");
    private final MultiLangComponent multilangComponent = DaggerMultiLangComponent.builder()
        .platformComponent(platformComponent)
        .build();
    private final AnalysisContextService analysisContextService = multilangComponent.getAnalysisContextService();

    private final SmlInstantiateGlobalScope instantiateGlobalScope = new SmlInstantiateGlobalScope();
    private final SmlPartialSolveProject partialSolveProject = new SmlPartialSolveProject();
    private final SmlPartialSolveFile partialSolveFile = new SmlPartialSolveFile(languageComponent.getStrategoRuntimeBuilder().
        build().getTermFactory(), platformComponent.getLoggerFactory());
    private final SmlAnalyzeProject analyzeProject = new SmlAnalyzeProject(instantiateGlobalScope, partialSolveProject,
        partialSolveFile, buildContextConfiguration, specFunction, analysisContextService, platformComponent.getLoggerFactory());
    private final SmlBuildMessages buildMessages = new SmlBuildMessages(analyzeProject, readConfigYaml, buildContextConfiguration, analysisContextService);

    private final Level logLevel = Level.Info;
    private final HashSet<ResourceKey> resources = new HashSet<>();

    private final LanguageId languageId = new LanguageId("sdf3");
    private final ContextId contextId = new ContextId("AnalysisTest");

    @Test void testSingleError() throws ExecException, InterruptedException {
        final TextResource resource = createTextResource("module a syntax A = B", "a.sdf3");
        resources.add(resource.getKey());

        AnalysisContext context = analysisContextService.getAnalysisContext(contextId);

        try(MixedSession session = context.createPieForContext().newSession()) {
            KeyedMessages messages = session.require(buildMessages
                .createTask(new SmlBuildMessages.Input(projectPath, context)));
            assertTrue(messages.containsError());
            assertEquals(1, messages.count());
            assertEquals(1, messages.getAllMessages().get(resource.getKey()).size());
        }
    }

    @Test void testSingleSuccess() throws ExecException, InterruptedException {
        final TextResource resource1 = createTextResource("module a", "a.sdf3");
        resources.add(resource1.getKey());

        // Loading spec
        AnalysisContext context = analysisContextService.getAnalysisContext(contextId);

        try(MixedSession session = context.createPieForContext().newSession()) {
            KeyedMessages messages = session.require(buildMessages
                .createTask(new SmlBuildMessages.Input(projectPath, context)));
            assertEquals(0, messages.count());
        }
    }

    @Test void testMultipleErrors() throws ExecException, InterruptedException {
        final TextResource resource1 = createTextResource("module a syntax B = A", "a.sdf3");
        final TextResource resource2 = createTextResource("module b syntax C = A B", "b.sdf3");
        resources.add(resource1.getKey());
        resources.add(resource2.getKey());

        // Loading spec
        AnalysisContext context = analysisContextService.getAnalysisContext(contextId);

        try(MixedSession session = context.createPieForContext().newSession()) {
            KeyedMessages messages = session.require(buildMessages
                .createTask(new SmlBuildMessages.Input(projectPath, context)));
            assertTrue(messages.containsError());
            assertEquals(4, messages.count());
            assertTrue(messages.containsWarning());
            assertEquals(1, messages.getAllMessages().get(resource1.getKey()).size());
            assertEquals(3, messages.getAllMessages().get(resource2.getKey()).size());
        }
    }

    @Test void testMultipleSuccess() throws ExecException, InterruptedException {
        final TextResource resource1 = createTextResource("module a", "a.sdf3");
        final TextResource resource2 = createTextResource("module b", "b.sdf3");
        resources.add(resource1.getKey());
        resources.add(resource2.getKey());

        // Loading spec
        AnalysisContext context = analysisContextService.getAnalysisContext(contextId);

        try(MixedSession session = context.createPieForContext().newSession()) {
            KeyedMessages messages = session.require(buildMessages
                .createTask(new SmlBuildMessages.Input(projectPath, context)));
            assertFalse(messages.containsError());
            assertEquals(0, messages.count());
        }
    }

    @Test void testMutualResolve() throws ExecException, InterruptedException {
        final TextResource resource1 = createTextResource("module a syntax A = \"\"", "a.sdf3");
        final TextResource resource2 = createTextResource("module b imports a syntax B = A", "b.sdf3");
        final TextResource resource3 = createTextResource("module c imports a b syntax C = A syntax C = B", "c.sdf3");
        resources.add(resource1.getKey());
        resources.add(resource2.getKey());
        resources.add(resource3.getKey());

        // Loading spec
        AnalysisContext context = analysisContextService.getAnalysisContext(contextId);

        try(MixedSession session = context.createPieForContext().newSession()) {
            KeyedMessages messages = session.require(buildMessages
                .createTask(new SmlBuildMessages.Input(projectPath, context)));
            assertFalse(messages.containsError());
            assertEquals(0, messages.count());
        }
    }

    @BeforeEach public void createAnalysisContext() throws IOException {
        ResourceKeyString id = ResourceKeyString.of("mb/sdf3/src-gen/statix");
        ClassLoaderResource statixSpec = statixRegistry.getResource(id);
        SpecBuilder spec = SpecUtils.loadSpec(statixSpec, "statix/statics", termFactory);

        Function<ResourceKey, IStrategoTerm> preAnalyze = languageComponent.getPreStatix().createFunction()
            .mapInput((exec, key) -> languageComponent.getIndexAst().createSupplier(key));

        LanguageMetadata languageMetadata = ImmutableLanguageMetadata.builder()
            .languageId(languageId)
            .statixSpec(spec)
            .fileConstraint("statix/statics!moduleOK")
            .projectConstraint("statix/statics!projectOK")
            .resourcesSupplier((c, p) -> resources)
            .astFunction(preAnalyze)
            .addTaskDefs(instantiateGlobalScope, partialSolveProject, partialSolveFile, analyzeProject, buildMessages,
                languageComponent.getPreStatix(), languageComponent.getIndexAst(),
                languageComponent.getPostStatix(), parse)
            .addResourceRegistries()
            .postTransform(languageComponent.getPostStatix().createFunction())
            .build();

        ContextConfig config = new ContextConfig();
        config.setLanguages(Lists.newArrayList(languageId));
        config.setLogLevel(logLevel.toString());

        analysisContextService.registerLanguageLoader(languageId, () -> languageMetadata);
        analysisContextService.registerContextConfig(contextId, config);
        analysisContextService.initializeService();
    }
}
