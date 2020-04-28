package mb.mod.spoofax.task;

import mb.common.util.ListView;
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
import mb.spoofax.core.language.command.CommandOutput;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

public class ModShowScopeGraph implements TaskDef<ModShowScopeGraph.Args, CommandOutput> {
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
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final StrategoRuntime prototypeStrategoRuntime;

    @Inject public ModShowScopeGraph(
        ModParse parse,
        ModAnalyzeMulti analyze,
        ResourceService resourceService,
        StrategoRuntimeBuilder strategoRuntimeBuilder,
        StrategoRuntime prototypeStrategoRuntime
    ) {
        this.parse = parse;
        this.analyze = analyze;
        this.resourceService = resourceService;
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.prototypeStrategoRuntime = prototypeStrategoRuntime;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandOutput exec(ExecContext context, Args args) throws Exception {
        final ResourceWalker walker = new PathResourceWalker(new NoHiddenPathMatcher());
        final ResourceMatcher matcher = new PathResourceMatcher(new ExtensionPathMatcher("mod"));
        final ModAnalyzeMulti.@Nullable Output output = context.require(analyze, new ModAnalyzeMulti.Input(args.project, walker, matcher, parse.createAstFunction()));
        if(output == null) {
            throw new RuntimeException("Cannot show scope graph, analysis output for '" + args.project + "' is null");
        }
        final ConstraintAnalyzer.@Nullable Result analysisResult = output.result.getResult(args.file);
        if(analysisResult == null) {
            throw new RuntimeException("Cannot show scope graph, analysis result for '" + args.file + "' is null");
        }
        if(analysisResult.ast == null) {
            throw new RuntimeException("Cannot show scope graph, analyzed AST for '" + args.file + "' is null");
        }

        final StrategoRuntime strategoRuntime = strategoRuntimeBuilder.buildFromPrototype(prototypeStrategoRuntime);
        final String strategyId = "stx--show-scopegraph";
        final ITermFactory termFactory = strategoRuntime.getTermFactory();
        final IStrategoTerm inputTerm = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), analysisResult.ast, resourceService.toString(args.file), resourceService.toString(args.project));
        final @Nullable IStrategoTerm result = strategoRuntime.addContextObject(output.context).invoke(strategyId, inputTerm);
        if(result == null) {
            throw new RuntimeException("Cannot show scope graph, executing Stratego strategy '" + strategyId + "' failed");
        }

        final String formatted = StrategoUtil.toString(result);
        return new CommandOutput(ListView.of(CommandFeedback.showText(formatted, "Scope graph for '" + args.file + "'")));
    }

    @Override public Task<CommandOutput> createTask(Args args) {
        return TaskDef.super.createTask(args);
    }
}
