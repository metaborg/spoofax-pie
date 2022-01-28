package mb.statix.common;

import mb.statix.spoofax.IStatixProjectConfig;
import mb.statix.spoofax.SolverMode;
import mb.stratego.common.AdaptException;
import mb.stratego.common.AdaptableContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.CallT;
import org.spoofax.interpreter.stratego.PrimT;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import java.util.Optional;

public class StatixConcurrentEnabledPrimitive extends StatixCommonPrimitive {

    public StatixConcurrentEnabledPrimitive() {
        super("STX_is_concurrent_enabled", 0, 0);
    }

    @Override
    public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        try {
            // Call language_components primitive defined in spoofax2.common
            Optional<String> qualifiedArtifactId = getQualifiedLanguageId(env);
            if(!qualifiedArtifactId.isPresent()) {
                return false;
            }
            IStatixProjectConfig config = AdaptableContext.adaptContextObject(env.contextObject(), IStatixProjectConfig.class);
            return config.languageMode(qualifiedArtifactId.get(), SolverMode.DEFAULT).concurrent;
        } catch(AdaptException e) {
            return false;
        }
    }
}
