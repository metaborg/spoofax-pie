package mb.mod;

import mb.common.message.Severity;
import mb.constraint.common.ConstraintAnalyzer;
import mb.constraint.common.ConstraintAnalyzer.MultiFileResult;
import mb.constraint.common.ConstraintAnalyzer.SingleFileResult;
import mb.constraint.common.ConstraintAnalyzerContext;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.resource.DefaultResourceKey;
import mb.resource.ResourceKey;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ModAnalyzerTest extends ModTestBase {
    @Test void analyzeSingleErrors() throws Exception {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.mod");
        final JSGLR1ParseOutput parsed = parser.parse("let a = mod {}; dbg a.b;", startSymbol, resource);
        final SingleFileResult result = analyzer.analyze(rootKey, resource, parsed.ast, new ConstraintAnalyzerContext());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.containsError());
    }

    @Test void analyzeSingleSuccess() throws Exception {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.mod");
        final JSGLR1ParseOutput parsed = parser.parse("let a = mod { let b = 1; }; dbg a.b;", startSymbol, resource);
        final SingleFileResult result = analyzer.analyze(rootKey, resource, parsed.ast, new ConstraintAnalyzerContext());
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.isEmpty());
    }

    @Test void analyzeMultipleErrors() throws Exception {
        final ResourceKey resource1 = new DefaultResourceKey(qualifier, "a.mod");
        final JSGLR1ParseOutput parsed1 = parser.parse("let a = 1;", startSymbol, resource1);
        final ResourceKey resource2 = new DefaultResourceKey(qualifier, "b.mod");
        final JSGLR1ParseOutput parsed2 = parser.parse("let b = 2;", startSymbol, resource2);
        final ResourceKey resource3 = new DefaultResourceKey(qualifier, "c.mod");
        final JSGLR1ParseOutput parsed3 = parser.parse("let c = d;", startSymbol, resource3);
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(resource1, parsed1.ast);
        asts.put(resource2, parsed2.ast);
        asts.put(resource3, parsed3.ast);
        final MultiFileResult result = analyzer.analyze(rootKey, asts, new ConstraintAnalyzerContext());
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
        boolean foundCorrectMessage = result.messages.getMessagesWithKey().stream()
            .filter(msg -> resource3.equals(msg.getKey()))
            .flatMap(msg -> msg.getValue().stream())
            .anyMatch(msg -> msg.severity.equals(Severity.Error));
        assertTrue(foundCorrectMessage);
    }

    @Test void analyzeMultipleSuccess() throws Exception {
        final ResourceKey resource1 = new DefaultResourceKey(qualifier, "a.tig");
        final JSGLR1ParseOutput parsed1 = parser.parse("let a = 1;", startSymbol, resource1);
        final ResourceKey resource2 = new DefaultResourceKey(qualifier, "b.tig");
        final JSGLR1ParseOutput parsed2 = parser.parse("let b = 1;", startSymbol, resource2);
        final ResourceKey resource3 = new DefaultResourceKey(qualifier, "c.tig");
        final JSGLR1ParseOutput parsed3 = parser.parse("let c = 1;", startSymbol, resource3);
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>();
        asts.put(resource1, parsed1.ast);
        asts.put(resource2, parsed2.ast);
        asts.put(resource3, parsed3.ast);
        final MultiFileResult result = analyzer.analyze(rootKey, asts, new ConstraintAnalyzerContext());
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

    @Test void showScopeGraph() throws Exception, StrategoException {
        final ResourceKey resource = new DefaultResourceKey(qualifier, "a.mod");
        final JSGLR1ParseOutput parsed = parser.parse("let a = 1;", startSymbol, resource);
        final ConstraintAnalyzerContext constraintAnalyzerContext = new ConstraintAnalyzerContext();
        final SingleFileResult result = analyzer.analyze(rootKey, resource, parsed.ast, constraintAnalyzerContext);
        assertNotNull(result.ast);
        assertNotNull(result.analysis);
        assertTrue(result.messages.isEmpty());
        final IStrategoTerm input = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), result.ast, resource.toString(), rootKey.toString());
        final @Nullable IStrategoTerm output = strategoRuntime.addContextObject(constraintAnalyzerContext).invoke("stx--show-scopegraph", input);
        assertNotNull(output);
    }
}
