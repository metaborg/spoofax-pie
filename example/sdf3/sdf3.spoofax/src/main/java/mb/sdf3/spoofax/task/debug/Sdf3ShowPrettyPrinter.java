package mb.sdf3.spoofax.task.debug;

import mb.sdf3.spoofax.task.Sdf3DesugarTemplates;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToPrettyPrinter;
import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Inject;
import javax.inject.Provider;

@LanguageScope
public class Sdf3ShowPrettyPrinter extends ShowTaskDef {
    @Inject public Sdf3ShowPrettyPrinter(
        Sdf3Parse parse,
        Sdf3DesugarTemplates desugarTemplates,
        Sdf3ToPrettyPrinter toPrettyPrinter,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        super(parse, desugarTemplates, toPrettyPrinter, strategoRuntimeProvider, "pp-stratego-string", "pretty-printer");
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
