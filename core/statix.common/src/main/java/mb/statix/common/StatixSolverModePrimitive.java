package mb.statix.common;

import mb.nabl2.terms.stratego.StrategoBlob;
import mb.statix.spoofax.IStatixProjectConfig;
import mb.statix.spoofax.SolverMode;
import mb.stratego.common.AdaptException;
import mb.stratego.common.AdaptableContext;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.Optional;

public class StatixSolverModePrimitive extends StatixCommonPrimitive {

    public StatixSolverModePrimitive() {
        super("STX_solver_mode", 0, 0);
    }

    @Override
    public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        try {
            Optional<String> qualifiedLanguageId = getQualifiedLanguageId(env);
            if(!qualifiedLanguageId.isPresent()) {
                return false;
            }
            IStatixProjectConfig config = AdaptableContext.adaptContextObject(env.contextObject(), IStatixProjectConfig.class);
            SolverMode mode = config.languageMode(qualifiedLanguageId.get(), SolverMode.DEFAULT);
            env.setCurrent(new StrategoBlob(mode));
            return true;
        } catch(AdaptException e) {
            return false;
        }
    }
}
