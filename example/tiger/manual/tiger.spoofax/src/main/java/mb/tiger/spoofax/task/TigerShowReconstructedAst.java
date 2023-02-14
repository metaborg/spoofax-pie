package mb.tiger.spoofax.task;

import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.tiger.spoofax.task.reusable.TigerConstructTextualChange;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;

public class TigerShowReconstructedAst implements TaskDef<ResourceKey, CommandFeedback> {

    private final TigerParse parse;
    private final TigerConstructTextualChange constructTextualChange;

    @Inject
    public TigerShowReconstructedAst(TigerParse parse, TigerConstructTextualChange constructTextualChange) {
        this.parse = parse;
        this.constructTextualChange = constructTextualChange;
    }

    @Override
    public String getId() {
        return TigerShowReconstructedAst.class.getSimpleName();
    }

    @Override
    public CommandFeedback exec(ExecContext context, ResourceKey file) throws Exception {
        final Supplier<Result<IStrategoTerm, JsglrParseException>> astSupplier = parse.inputBuilder().withFile(file).buildAstSupplier();

        return context.require(constructTextualChange.createSupplier(astSupplier)).mapOrElse(
            res -> CommandFeedback.of(ShowFeedback.showText(TermUtils.toJavaString(res.getSubterm(2)), "result")),
            err -> CommandFeedback.of(err)
        );
    }
}
