package mb.statix.task;

import mb.aterm.common.TermToString;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext;
import mb.statix.StatixClassLoaderResources;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.Serializable;

public class StatixEvaluateTest implements TaskDef<StatixEvaluateTest.Args, CommandFeedback> {
    public static class Args implements Serializable {
        public final ResourcePath rootDirectory;
        public final ResourceKey file;

        public Args(ResourcePath rootDirectory, ResourceKey file) {
            this.rootDirectory = rootDirectory;
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final StatixEvaluateTest.Args args = (StatixEvaluateTest.Args)o;
            if(!rootDirectory.equals(args.rootDirectory)) return false;
            return file.equals(args.file);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + file.hashCode();
            return result;
        }

        @Override public String toString() {
            return "StatixEvaluateTest.Args{" +
                "rootDirectory=" + rootDirectory +
                ", file=" + file +
                '}';
        }
    }


    private final StatixClassLoaderResources classLoaderResources;
    private final StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider;
    private final StatixAnalyzeFile analyzeFile;


    @Inject public StatixEvaluateTest(
        StatixClassLoaderResources classLoaderResources,
        StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider,
        StatixAnalyzeFile analyzeFile
    ) {
        this.classLoaderResources = classLoaderResources;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
        this.analyzeFile = analyzeFile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final ResourcePath rootDirectory = input.rootDirectory;
        final ResourceKey file = input.file;
        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();
        return context
            .require(analyzeFile, new StatixAnalyzeFile.Input(rootDirectory, file))
            .mapCatching(output -> {
                final IStrategoTerm result = strategoRuntime
                    .addContextObject(output.context)
                    .addContextObject(new Spoofax2ProjectContext(input.rootDirectory))
                    .invoke("editor-evaluate-traditional", StrategoUtil.createLegacyBuilderInputTerm(
                        strategoRuntime.getTermFactory(),
                        output.ast,
                        file.asString(),
                        rootDirectory.asString()
                    ));
                if(result.getSubtermCount() > 1) { // Should return (filename, result) tuple -- interested in result.
                    return TermToString.toString(result.getSubterm(1));
                } else {
                    return TermToString.toString(result);
                }
            })
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Test result for '" + file + "'")), e -> CommandFeedback.ofTryExtractMessagesFrom(e, file));
    }
}
