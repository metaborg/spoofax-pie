package mb.mod.spoofax.task;

import mb.common.result.Result;
import mb.constraint.common.ConstraintAnalyzer;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionPathMatcher;
import mb.resource.hierarchical.match.path.NoHiddenPathMatcher;
import mb.resource.hierarchical.walk.PathResourceWalker;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;

public class ModShowScopeGraph implements TaskDef<ModShowScopeGraph.Args, CommandFeedback> {
    public static class Args implements Serializable {
        public final ResourcePath project;
        public final ResourceKey file;

        public Args(ResourcePath project, ResourceKey file) {
            this.project = project;
            this.file = file;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return project.equals(args.project) &&
                file.equals(args.file);
        }

        @Override public int hashCode() {
            return Objects.hash(project, file);
        }

        @Override public String toString() {
            return "Args{" +
                "project=" + project +
                ", file=" + file +
                '}';
        }
    }

    private final ModParse parse;
    private final ModAnalyzeMulti analyze;
    private final ResourceService resourceService;
    private final Provider<StrategoRuntime> strategoRuntimeProvider;

    @Inject public ModShowScopeGraph(
        ModParse parse,
        ModAnalyzeMulti analyze,
        ResourceService resourceService,
        Provider<StrategoRuntime> strategoRuntimeProvider
    ) {
        this.parse = parse;
        this.analyze = analyze;
        this.resourceService = resourceService;
        this.strategoRuntimeProvider = strategoRuntimeProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) throws Exception {
        final ResourceWalker walker = new PathResourceWalker(new NoHiddenPathMatcher());
        final ResourceMatcher matcher = new PathResourceMatcher(new ExtensionPathMatcher("mod"));
        final ResourceKey file = args.file;
        return context.require(analyze, new ModAnalyzeMulti.Input(args.project, walker, matcher, parse.createRecoverableAstFunction()))
            .flatMapOrElse((output) -> {
                final ConstraintAnalyzer.@Nullable Result result = output.result.getResult(file);
                if(result != null && result.ast != null) {
                    return Result.ofOk(output);
                } else {
                    return Result.ofErr(new Exception("Cannot show scope graph, analyzed result or AST for '" + file + "' is null"));
                }
            }, Result::ofErr)
            .mapCatching(output -> {
                final StrategoRuntime strategoRuntime = strategoRuntimeProvider.get().addContextObject(output.context);
                final ITermFactory termFactory = strategoRuntime.getTermFactory();
                final IStrategoTerm inputTerm = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), output.result.getResult(file).ast, resourceService.toString(args.file), resourceService.toString(args.project));
                return StrategoUtil.toString(strategoRuntime.invoke("stx--show-scopegraph", inputTerm));
            })
            .mapOrElse(text -> CommandFeedback.of(ShowFeedback.showText(text, "Scope graph for '" + file + "'")), e -> CommandFeedback.ofTryExtractMessagesFrom(e, file));
    }

    @Override public Task<CommandFeedback> createTask(Args args) {
        return TaskDef.super.createTask(args);
    }
}
