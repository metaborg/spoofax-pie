package mb.sdf3.task.debug;

import mb.common.util.StringUtil;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.resource.ResourceKey;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;

class ProvideOutputShared {
    final Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider;
    final String prettyPrintStrategy;
    final String resultName;

    ProvideOutputShared(Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider, String prettyPrintStrategy, String resultName) {
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
        this.prettyPrintStrategy = prettyPrintStrategy;
        this.resultName = resultName;
    }

    CommandFeedback provideOutput(ExecContext context, boolean concrete, IStrategoTerm ast, ResourceKey file) {
        if(concrete) {
            try {
                final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();
                final IStrategoTerm text = strategoRuntime.invoke(prettyPrintStrategy, ast);
                return CommandFeedback.of(ShowFeedback.showText(StrategoUtil.toString(text), StringUtil.capitalize(resultName) + " (concrete) of '" + file + "'"));
            } catch(StrategoException e) {
                return CommandFeedback.of(new Exception("Pretty-printing '" + resultName + "' AST failed", e));
            }
        } else {
            return CommandFeedback.of(ShowFeedback.showText(StrategoUtil.toString(ast), StringUtil.capitalize(resultName) + " (abstract) of '" + file + "'"));
        }
    }
}
