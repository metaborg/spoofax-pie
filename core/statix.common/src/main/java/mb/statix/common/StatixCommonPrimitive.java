package mb.statix.common;

import mb.statix.spoofax.IStatixProjectConfig;
import mb.statix.spoofax.SolverMode;
import mb.stratego.common.AdaptException;
import mb.stratego.common.AdaptableContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.PrimT;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import java.util.Optional;

public abstract class StatixCommonPrimitive extends AbstractPrimitive  {
    public StatixCommonPrimitive(String name, int svars, int tvars) {
        super(name, svars, tvars);
    }

    protected Optional<String> getQualifiedLanguageId(IContext env) throws InterpreterException {
        // Call language_components primitive defined in spoofax2.common
        IStrategoTerm current = env.current();
        if(!new PrimT("language_components", new Strategy[0], new IStrategoTerm[0]).evaluate(env)) {
            return Optional.empty();
        }

        IStrategoTerm languageComponentsTerm = env.current();
        env.setCurrent(current);

        if(!TermUtils.isList(languageComponentsTerm, 1)) {
            return Optional.empty();
        }

        IStrategoTerm languageComponentTerm = languageComponentsTerm.getSubterm(0);
        if(!TermUtils.isTuple(languageComponentTerm)) {
            return Optional.empty();
        }

        IStrategoTerm artifactIdTerm = languageComponentTerm.getSubterm(1);
        if(!TermUtils.isString(artifactIdTerm)) {
            return Optional.empty();
        }
        String artifactId = ((IStrategoString) artifactIdTerm).stringValue();

        IStrategoTerm groupIdTerm = languageComponentTerm.getSubterm(0);
        if(!TermUtils.isString(groupIdTerm)) {
            return Optional.empty();
        }
        String groupId = ((IStrategoString) groupIdTerm).stringValue();

        String qualifiedArtifactId = groupId + ":" + artifactId;
        return Optional.of(qualifiedArtifactId);
    }

    protected Optional<SolverMode> getSolverMode(IContext env) throws InterpreterException, AdaptException {
        Optional<String> qualifiedLanguageId = getQualifiedLanguageId(env);
        if(!qualifiedLanguageId.isPresent()) {
            return Optional.empty();
        }
        IStatixProjectConfig config = AdaptableContext.adaptContextObject(env.contextObject(), IStatixProjectConfig.class);
        SolverMode mode = config.languageMode(qualifiedLanguageId.get(), SolverMode.DEFAULT);
        return Optional.of(mode);
    }
}
