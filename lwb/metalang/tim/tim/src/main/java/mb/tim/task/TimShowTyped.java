package mb.tim.task;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.WritableResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

public class TimShowTyped implements TaskDef<TimShowTyped.Args, CommandFeedback> {
    public static final class Args implements Serializable {
        public final ResourcePath rootDirectory;
        public final ResourcePath file;

        public Args(ResourcePath rootDirectory, ResourcePath file) {
            this.rootDirectory = rootDirectory;
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            TimShowTyped.Args input = (TimShowTyped.Args)o;
            if(!rootDirectory.equals(input.rootDirectory)) return false;
            return file.equals(input.file);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + file.hashCode();
            return result;
        }

        @Override public String toString() {
            return "TimShowSimplified.Args{" +
                "rootDirectory=" + rootDirectory +
                ", file=" + file +
                '}';
        }
    }

    private final TimAnalyzeFile analyzeFile;
    private final TimGetStrategoRuntimeProvider getStrategoRuntimeProvider;

    @Inject
    public TimShowTyped(TimAnalyzeFile analyzeFile, TimGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        this.analyzeFile = analyzeFile;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
    }

    @Override public CommandFeedback exec(ExecContext context, TimShowTyped.Args args) throws Exception {
        final ResourcePath file = args.file;
        return context.require(analyzeFile, new TimAnalyzeFile.Input(args.rootDirectory, file)).mapOrElse(
            output -> {
                try {
                    final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get().addContextObject(output.context);
                    final IStrategoTerm result = strategoRuntime.invoke("typed-tim", output.ast);
                    if (!(result instanceof IStrategoString)) {
                        return CommandFeedback.of(ShowFeedback.showText("Expected result of compilation to be a string", "Compilation error for '" + file + "'"));
                    }
                    final ResourcePath outputFilePath = file.replaceLeafExtension("typed.tim");
                    final WritableResource outputFile = context.getWritableResource(outputFilePath);
                    outputFile.writeString(((IStrategoString)result).stringValue());
//                    context.provide(outputFile);
                    // TODO: Figure out how to get rid of the "hidden dependency" error
                    return CommandFeedback.of(ShowFeedback.showFile(outputFilePath));
                } catch(StrategoException e) {
                    final StringBuilder errorSB = new StringBuilder();

                    Throwable ex = e;
                    while(ex != null) {
                        errorSB.append(ex.getMessage()).append("\n");
                        ex = ex.getCause();
                    }
                    return CommandFeedback.of(ShowFeedback.showText(errorSB.toString(), "Compilation error for '" + file + "'"));
                } catch(IOException e) {
                    return CommandFeedback.of(ShowFeedback.showText(e.toString(), "Compilation error for '" + file + "'"));
                }
            },
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
        );
    }

    @Override public String getId() {
        return getClass().getName();
    }
}
