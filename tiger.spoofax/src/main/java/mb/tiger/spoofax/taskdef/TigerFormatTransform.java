package mb.tiger.spoofax.taskdef;

import mb.common.util.ListView;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.transform.FileSubject;
import mb.spoofax.core.language.transform.TransformInput;
import mb.spoofax.core.language.transform.TransformOutput;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.spoofax.interpreter.library.IOAgent;

import javax.inject.Inject;

public class TigerFormatTransform implements TaskDef<TransformInput, TransformOutput> {
    private final TigerParse parse;
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final StrategoRuntime prototypeStrategoRuntime;

    @Inject public TigerFormatTransform(
        TigerParse parse,
        StrategoRuntimeBuilder strategoRuntimeBuilder,
        StrategoRuntime prototypeStrategoRuntime
    ) {
        this.parse = parse;
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.prototypeStrategoRuntime = prototypeStrategoRuntime;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput input) throws Exception {
        if(!(input.subject instanceof FileSubject)) {
            throw new RuntimeException("Cannot format, subject '" + input.subject + "' is not a file subject");
        }
        final ResourceKey file = ((FileSubject) input.subject).getFile();
        final JSGLR1ParseResult parseOutput = context.require(parse, file);
        final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.buildFromPrototype(prototypeStrategoRuntime);
        // TODO: create input term.
        strategoRuntime.invoke("editor-format", parseOutput.ast, new IOAgent());
        // TODO: return feedback.
        return new TransformOutput(ListView.of());
    }
}
