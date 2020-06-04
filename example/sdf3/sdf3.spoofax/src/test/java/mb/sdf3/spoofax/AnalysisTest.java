package mb.sdf3.spoofax;

import mb.common.message.KeyedMessages;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.nabl2.terms.matching.TermMatch;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.nabl2.terms.unification.OccursException;
import mb.nabl2.util.Set2;
import mb.pie.api.ExecException;
import mb.pie.api.Function;
import mb.pie.api.MixedSession;
import mb.pie.api.Session;
import mb.pie.api.Supplier;
import mb.pie.api.ValueSupplier;
import mb.resource.DefaultResourceKey;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourcePath;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.text.TextResource;
import mb.sdf3.Sdf3ClassloaderResources;
import mb.statix.common.context.AnalysisContext;
import mb.statix.common.context.AnalysisContextService;
import mb.statix.common.context.LanguageMetadata;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;
import mb.statix.utils.SpecUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

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
        final TextResource resource1 = createTextResource("module b", "a.sdf3");
        final TextResource resource2 = createTextResource("module a", "b.sdf3");
        resources.add(resource1.getKey());
        resources.add(resource2.getKey());

        // Loading spec
        AnalysisContext context = createAnalysisContext(resources);

        try (MixedSession session = newSession()) {
            KeyedMessages messages = session.require(context.createAnalyzerTask());
            assertEquals(2, messages.getAllMessages().size());
        }
    }

    @Test void testMultipleError() throws IOException, OccursException, ExecException {
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
            .astSupplier(parse.createNullableRecoverableAstFunction())
            .build();

        AnalysisContext context = AnalysisContextService.getAnalysisContext("AnalysisTest");
        context.clear();
        context.register(languageMetadata);

        addStatixTaskDef(termFactory);
        return context;
    }
}
