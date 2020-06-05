package mb.sdf3.spoofax;

import mb.common.message.KeyedMessages;
import mb.nabl2.terms.unification.OccursException;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Function;
import mb.pie.api.MixedSession;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.Supplier;
import mb.pie.api.ValueSupplier;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.text.TextResource;
import mb.sdf3.Sdf3ClassloaderResources;
import mb.statix.common.context.AnalysisContext;
import mb.statix.common.context.AnalysisContextService;
import mb.statix.common.context.LanguageMetadata;
import mb.statix.spec.Spec;
import mb.statix.utils.SpecUtils;
import org.junit.jupiter.api.Test;
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
    protected ITermFactory termFactory = new TermFactory();

    @Test void testSingleError() throws IOException, OccursException, ExecException {
        final HashSet<ResourceKey> resources = new HashSet<>();
        final TextResource resource = createTextResource("module test context-free syntax A = <A>", "a.sdf3");
        resources.add(resource.getKey());

        // Loading spec
        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = newSession()) {
            KeyedMessages messages = session.require(context.createAnalyzerTask());
            assertTrue(messages.containsError());
        }
    }

    @Test void testSingleSuccess() throws IOException, OccursException, ExecException {
        final HashSet<ResourceKey> resources = new HashSet<>();
        final TextResource resource1 = createTextResource("module a", "a.sdf3");
        resources.add(resource1.getKey());

        // Loading spec
        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = newSession()) {
            KeyedMessages messages = session.require(context.createAnalyzerTask());
            assertEquals(0, messages.getAllMessages().size());
        }
    }

    @Test void testMultipleError() throws IOException, OccursException, ExecException {
        final HashSet<ResourceKey> resources = new HashSet<>();
        final TextResource resource1 = createTextResource("module b context-free syntax A = <A>", "a.sdf3");
        final TextResource resource2 = createTextResource("module a context-free syntax B = <B>", "b.sdf3");
        resources.add(resource1.getKey());
        resources.add(resource2.getKey());

        // Loading spec
        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = newSession()) {
            KeyedMessages messages = session.require(context.createAnalyzerTask());
            assertTrue(messages.containsError());
        }
    }

    @Test void testMultipleSuccess() throws IOException, OccursException, ExecException {
        final HashSet<ResourceKey> resources = new HashSet<>();
        final TextResource resource1 = createTextResource("module a", "a.sdf3");
        final TextResource resource2 = createTextResource("module b", "b.sdf3");
        resources.add(resource1.getKey());
        resources.add(resource2.getKey());

        // Loading spec
        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = newSession()) {
            KeyedMessages messages = session.require(context.createAnalyzerTask());
            assertFalse(messages.containsError());
        }
    }

    @Test void testMutualResolve() throws IOException, OccursException, ExecException {
        final HashSet<ResourceKey> resources = new HashSet<>();
        final TextResource resource1 = createTextResource("module a syntax A = \"\"", "a.sdf3");
        final TextResource resource2 = createTextResource("module b imports a syntax B = A", "b.sdf3");
        final TextResource resource3 = createTextResource("module c imports a b syntax C = A syntax C = B", "c.sdf3");
        resources.add(resource1.getKey());
        resources.add(resource2.getKey());
        resources.add(resource3.getKey());

        // Loading spec
        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = newSession()) {
            KeyedMessages messages = session.require(context.createAnalyzerTask());
            assertFalse(messages.containsError());
        }
    }

    private AnalysisContext createAnalysisContext(HashSet<ResourceKey> resources) throws IOException, OccursException {
        ResourceKeyString id = ResourceKeyString.of("mb/sdf3/src-gen/statix");
        ClassLoaderResource statixSpec = statixRegistry.getResource(id);
        Spec spec = new SpecUtils(termFactory).loadSpec(statixSpec, "statix/statics");

        LanguageMetadata languageMetadata = LanguageMetadata.builder()
            .languageId("sdf3")
            .statixSpec(spec)
            .fileConstraint("statix/statics!moduleOK")
            .projectConstraint("statix/statics!projectOK")
            .resourcesSupplier(new ValueSupplier<>(resources))
            .astFunction(languageComponent.getPreStatix().createFunction()
                .mapInput(new ReadResourceFunction(parse.createRecoverableAstFunction())
                    .mapOutput(ValueSupplier::new)))
            .build();

        AnalysisContext context = AnalysisContextService.getAnalysisContext("AnalysisTest");
        context.clear();
        context.register(languageMetadata);

        addStatixTaskDef(termFactory);
        return context;
    }

    static class ReadResourceFunction implements Function<ResourceKey, IStrategoTerm> {
        private final Function<Supplier<String>, IStrategoTerm> astFunction;

        public ReadResourceFunction(Function<Supplier<String>, IStrategoTerm> astFunction) {
            this.astFunction = astFunction;
        }

        @Override
        public IStrategoTerm apply(ExecContext context, ResourceKey input) throws ExecException, InterruptedException {
            return astFunction.apply(context, new ResourceStringSupplier(input));
        }
    }
}
