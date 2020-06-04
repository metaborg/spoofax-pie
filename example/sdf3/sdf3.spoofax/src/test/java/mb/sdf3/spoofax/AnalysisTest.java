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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnalysisTest extends TestBase {

    protected final ClassLoaderResourceRegistry statixRegistry = Sdf3ClassloaderResources.createClassLoaderResourceRegistry();
    protected ITermFactory termFactory = new TermFactory();

    @Test void testSingleNoError() throws IOException, OccursException, ExecException {
        final TextResource resource = createTextResource("module test context-free syntax A = <A>", "a.sdf3");
        final HashSet<ResourceKey> resources = new HashSet<>();
        resources.add(resource.getKey());

        // Loading spec
        ResourceKeyString id = ResourceKeyString.of("mb/sdf3/src-gen/statix");
        ClassLoaderResource statixSpec = statixRegistry.getResource(id);
        Spec spec = new SpecUtils(termFactory).loadSpec(statixSpec, "statix/statics");

        LanguageMetadata languageMetadata = LanguageMetadata.builder()
            .languageId("sdf3")
            .statixSpec(spec)
            .fileConstraint("statix/statics!moduleOK")
            .projectConstraint("statix/statics!projectOk")
            .resourcesSupplier(new ValueSupplier<>(resources))
            .astSupplier(parse.createNullableRecoverableAstFunction())
            .build();

        AnalysisContext context = AnalysisContextService.getAnalysisContext("AnalysisTest.singleTestNoError");
        context.register(languageMetadata);

        addStatixTaskDef(termFactory);

        try (MixedSession session = newSession()) {
            KeyedMessages messages = session.require(context.createAnalyzerTask());
            assertTrue(messages.containsError());
        }
    }
}
