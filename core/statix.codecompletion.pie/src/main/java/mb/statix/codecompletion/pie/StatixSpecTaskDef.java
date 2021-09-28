package mb.statix.codecompletion.pie;

import com.google.common.collect.ListMultimap;
import mb.common.result.Result;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.ReadableResource;
import mb.statix.spec.Rule;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixTerms;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public abstract class StatixSpecTaskDef implements TaskDef<None, Result<Spec, ?>> {

    private final ITermFactory termFactory;

    protected StatixSpecTaskDef(
        ITermFactory termFactory
    ) {
        this.termFactory = termFactory;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public abstract Result<Spec, ?> exec(ExecContext context, None input) throws Exception;

    /**
     * Reads a resource into a {@link Spec}.
     *
     * @param resource the resource to read
     * @return maybe a {@link Spec}
     */
    protected Result<Spec, ?> readToSpec(ReadableResource resource) {
        try {
            final IStrategoTerm specAst;
            try(InputStream inputStream = resource.openRead()) {
                specAst = readTerm(inputStream);
            }
            @Nullable final Spec spec = toSpec(specAst);
            @Nullable final String overlappingRulesMsg = checkNoOverlappingRules(spec);
            if(overlappingRulesMsg != null) {
                // Invalid specification
                throw new IllegalStateException(overlappingRulesMsg);
            }
            return Result.ofOk(spec);
        } catch (Exception ex) {
            return Result.ofErr(ex);
        }
    }

    private IStrategoTerm readTerm(InputStream stream) {
        final TermReader reader = new TermReader(termFactory);
        try {
            return reader.parseFromStream(stream);
        } catch(IOException e) {
            throw new IllegalStateException("Loading ATerm from stream failed unexpectedly", e);
        }
    }

    /**
     * Gets the Statix specification from the specified term.
     *
     * @param specAst the specification term
     * @return the specification
     */
    private Spec toSpec(IStrategoTerm specAst) throws InterpreterException {
        final ITerm specTerm = new StrategoTerms(termFactory).fromStratego(specAst);
        return StatixTerms.spec().match(specTerm).orElseThrow(() -> new InterpreterException("Expected spec, got " + specTerm));
    }

    /**
     * Reports any overlapping rules in the specification.
     *
     * @param spec the specification to check
     * @return a String message when the specification has no overlapping rules;
     * otherwise, {@code null}.
     */
    private static @Nullable String checkNoOverlappingRules(Spec spec) {
        final ListMultimap<String, Rule> rulesWithEquivalentPatterns = spec.rules().getAllEquivalentRules();
        if(!rulesWithEquivalentPatterns.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Found rules with equivalent patterns.\n");
            for(Map.Entry<String, Collection<Rule>> entry : rulesWithEquivalentPatterns.asMap().entrySet()) {
                sb.append("Overlapping rules for: ").append(entry.getKey()).append("\n");
                for(Rule rule : entry.getValue()) {
                    sb.append("* ").append(rule).append("\n");
                }
            }
            return sb.toString();
        }
        return null;
    }
}
