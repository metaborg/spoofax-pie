package mb.statix.spoofax.task;

import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.spoofax.StatixScope;
import mb.statix.spoofax.util.StatixUtil;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;

@StatixScope
public class StatixCompile implements TaskDef<StatixCompile.Input, Result<IStrategoTerm, ?>> {
    public static class Input implements Serializable {
        public final ResourcePath root;
        public final ResourcePath resource;

        public Input(ResourcePath root, ResourcePath resource) {
            this.root = root;
            this.resource = resource;
        }
    }

    private final StatixParse parse;
    private final StatixAnalyzeMulti analyze;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public StatixCompile(
        StatixParse parse,
        StatixAnalyzeMulti analyze,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.parse = parse;
        this.analyze = analyze;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<IStrategoTerm, ?> exec(ExecContext context, Input input) throws Exception {
        final Supplier<Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> supplier = analyze.createSingleFileOutputSupplier(
            new ConstraintAnalyzeMultiTaskDef.Input(input.root, StatixUtil.createResourceWalker(), StatixUtil.createResourceMatcher(), parse.createAstFunction()),
            input.resource
        );
        return context.require(supplier).flatMapOrElse((output) -> {
            if(output.result.messages.containsError()) {
                return Result.ofErr(new Exception("Cannot compile Statix specification; analysis resulted in errors")); // TODO: better error/exception
            }
            try {
                final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(output.context);
                final IStrategoTerm term = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), output.result.ast, input.resource, input.root);
                return Result.ofOk(strategoRuntime.invoke("generate-aterm", term));
            } catch(StrategoException e) {
                return Result.ofErr(e);
            }
        }, Result::ofErr);
    }
}
