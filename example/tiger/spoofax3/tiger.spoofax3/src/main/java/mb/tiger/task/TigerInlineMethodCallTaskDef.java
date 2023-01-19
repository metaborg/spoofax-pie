package mb.tiger.task;

import mb.jsglr.pie.ShowParsedAstTaskDef;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.statix.referenceretention.pie.InlineMethodCallTaskDef;
import mb.stratego.common.StrategoRuntime;
import mb.tego.strategies.runtime.TegoRuntime;
import mb.tiger.TigerScope;

import javax.inject.Inject;
import javax.inject.Provider;

// TODO: This task needs to be registered somewhere
@TigerScope
public class TigerInlineMethodCallTaskDef extends InlineMethodCallTaskDef {
    private final mb.tiger.TigerClassLoaderResources classLoaderResources;

    @Inject public TigerInlineMethodCallTaskDef(
        mb.tiger.task.TigerParse parseTaskDef,
        mb.tiger.task.TigerAnalyze analyzeTaskDef,
        Provider<StrategoRuntime> strategoRuntimeProvider,
        TegoRuntime tegoRuntime,
        StrategoTerms strategoTerms,
        LoggerFactory loggerFactory,
        mb.tiger.TigerClassLoaderResources classLoaderResources
    ) {
        super(
            parseTaskDef,
            analyzeTaskDef,
            strategoRuntimeProvider,
            tegoRuntime,
            strategoTerms,
            loggerFactory
        );
        this.classLoaderResources = classLoaderResources;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, InlineMethodCallTaskDef.Input args) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        return super.exec(context, args);
    }
}
