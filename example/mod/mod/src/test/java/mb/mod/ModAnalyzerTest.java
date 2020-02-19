package mb.mod;

import mb.common.message.Severity;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzer.MultiFileResult;
import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.constraint.common.ConstraintAnalyzerException;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.resource.DefaultResourceKey;
import mb.resource.DefaultResourceService;
import mb.resource.DummyResourceRegistry;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.url.URLResourceRegistry;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ModAnalyzerTest {
    private static final String qualifier = "test";

    private final ModParser parser = new ModParserFactory().create();
    private final String startSymbol = "Start";
    private final LoggerFactory loggerFactory = new SLF4JLoggerFactory();
    private final ResourceService resourceService = new DefaultResourceService(new DummyResourceRegistry(qualifier), new FSResourceRegistry(), new URLResourceRegistry());
    private final StrategoRuntimeBuilder strategoRuntimeBuilder = new ModStrategoRuntimeBuilderFactory().create(loggerFactory, resourceService);
    private final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.build();
    private final ModConstraintAnalyzer analyzer = new ModConstraintAnalyzerFactory(loggerFactory, resourceService, strategoRuntime).create();
    private final ResourceKey rootKey = new DefaultResourceKey(qualifier, "root");

    @Test void analyzeSingleErrors() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.mod");
        final JSGLR1ParseResult parsed = parser.parse("let a = mod {}; dbg a.b;", startSymbol, resource);
        assertTrue(parsed.getAst().isPresent());
        final SingleFileResult result =
            analyzer.analyze(rootKey, resource, parsed.getAst().get(), new ConstraintAnalyzerContext(), new IOAgent());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.containsError());
    }

    @Test void analyzeSingleSuccess() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.mod");
        final JSGLR1ParseResult parsed = parser.parse("let a = mod { let b = 1; }; dbg a.b;", startSymbol, resource);
        assertTrue(parsed.getAst().isPresent());
        final SingleFileResult result =
            analyzer.analyze(rootKey, resource, parsed.getAst().get(), new ConstraintAnalyzerContext(), new IOAgent());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.isEmpty());
    }

    @Test void analyzeMultipleErrors() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource1 = new DefaultResourceKey(qualifier, "a.mod");
        final JSGLR1ParseResult parsed1 = parser.parse("let a = 1;", startSymbol, resource1);
        assertTrue(parsed1.getAst().isPresent());
        final ResourceKey resource2 = new DefaultResourceKey(qualifier, "b.mod");
        final JSGLR1ParseResult parsed2 = parser.parse("let b = 2;", startSymbol, resource2);
        assertTrue(parsed2.getAst().isPresent());
        final ResourceKey resource3 = new DefaultResourceKey(qualifier, "c.mod");
        final JSGLR1ParseResult parsed3 = parser.parse("let c = d;", startSymbol, resource3);
        assertTrue(parsed3.getAst().isPresent());
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(resource1, parsed1.getAst().get());
        asts.put(resource2, parsed2.getAst().get());
        asts.put(resource3, parsed3.getAst().get());
        final MultiFileResult result = analyzer.analyze(rootKey, asts, new ConstraintAnalyzerContext(), new IOAgent());
        final ConstraintAnalyzer.@Nullable Result result1 = result.getResult(resource1);
        assertNotNull(result1);
        assertNotNull(result1.ast);
        assertNotNull(result1.analysis);
        final ConstraintAnalyzer.@Nullable Result result2 = result.getResult(resource2);
        assertNotNull(result2);
        assertNotNull(result2.ast);
        assertNotNull(result2.analysis);
        final ConstraintAnalyzer.@Nullable Result result3 = result.getResult(resource3);
        assertNotNull(result3);
        assertNotNull(result3.ast);
        assertNotNull(result3.analysis);
        assertEquals(1, result.messages.size());
        assertTrue(result.messages.containsError());
        final boolean[] foundCorrectMessage = {false};
        result.messages.accept((text, exception, severity, resource, region) -> {
            if(resource3.equals(resource) && severity.equals(Severity.Error)) {
                foundCorrectMessage[0] = true;
                return false;
            }
            return true;
        });
        assertTrue(foundCorrectMessage[0]);
    }

    @Test void analyzeMultipleSuccess() throws InterruptedException, ConstraintAnalyzerException {
        final ResourceKey resource1 = new DefaultResourceKey(qualifier, "a.tig");
        final JSGLR1ParseResult parsed1 = parser.parse("let a = 1;", startSymbol, resource1);
        assertTrue(parsed1.getAst().isPresent());
        final ResourceKey resource2 = new DefaultResourceKey(qualifier, "b.tig");
        final JSGLR1ParseResult parsed2 = parser.parse("let b = 1;", startSymbol, resource2);
        assertTrue(parsed2.getAst().isPresent());
        final ResourceKey resource3 = new DefaultResourceKey(qualifier, "c.tig");
        final JSGLR1ParseResult parsed3 = parser.parse("let c = 1;", startSymbol, resource3);
        assertTrue(parsed3.getAst().isPresent());
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(resource1, parsed1.getAst().get());
        asts.put(resource2, parsed2.getAst().get());
        asts.put(resource3, parsed3.getAst().get());
        final MultiFileResult result = analyzer.analyze(rootKey, asts, new ConstraintAnalyzerContext(), new IOAgent());
        final ConstraintAnalyzer.@Nullable Result result1 = result.getResult(resource1);
        assertNotNull(result1);
        assertNotNull(result1.ast);
        assertNotNull(result1.analysis);
        final ConstraintAnalyzer.@Nullable Result result2 = result.getResult(resource2);
        assertNotNull(result2);
        assertNotNull(result2.ast);
        assertNotNull(result2.analysis);
        final ConstraintAnalyzer.@Nullable Result result3 = result.getResult(resource3);
        assertNotNull(result3);
        assertNotNull(result3.ast);
        assertNotNull(result3.analysis);
        assertTrue(result.messages.isEmpty());
    }

    @Test @Disabled("Using Statix analysis data after analysis is WIP")
    void showScopeGraph() throws InterruptedException, ConstraintAnalyzerException, StrategoException {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.mod");
        final JSGLR1ParseResult parsed = parser.parse("let a = 1;", startSymbol, resource);
        assertTrue(parsed.getAst().isPresent());
        final SingleFileResult result =
            analyzer.analyze(rootKey, resource, parsed.getAst().get(), new ConstraintAnalyzerContext(), new IOAgent());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.isEmpty());
        final IStrategoTerm input = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), result.ast, resource.toString(), rootKey.toString());
        strategoRuntime.invoke("stx--show-scopegraph", input, new IOAgent());
    }
}
