package mb.statix.task;

import com.google.common.collect.Lists;
import mb.aterm.common.InvalidAstShapeException;
import mb.aterm.common.TermToString;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeFile;
import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
import mb.statix.common.MessageUtils;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import mb.statix.spoofax.IStatixProjectConfig;
import mb.statix.spoofax.StatixTerms;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.task.NullCancel;
import org.metaborg.util.task.NullProgress;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static mb.nabl2.terms.build.TermBuild.B;

@StatixScope
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
    private final StatixCompileProject compileProject;


    @Inject public StatixEvaluateTest(
        StatixClassLoaderResources classLoaderResources,
        StatixGetStrategoRuntimeProvider getStrategoRuntimeProvider,
        StatixAnalyzeFile analyzeFile,
        StatixCompileProject compileProject
    ) {
        this.classLoaderResources = classLoaderResources;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
        this.analyzeFile = analyzeFile;
        this.compileProject = compileProject;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsLocalResource(Args.class), ResourceStampers.hashFile());

        final ResourcePath rootDirectory = input.rootDirectory;
        final ResourceKey file = input.file;
        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();

        final Result<ConstraintAnalyzeFile.Output, ?> analyzeFileResult = context.require(analyzeFile, new StatixAnalyzeFile.Input(rootDirectory, file));
        final Result<ListView<StatixCompileModule.Output>, ?> compileProjectResult = context.require(compileProject, input.rootDirectory);
        try {
            return analyzeFileResult.mapThrowingOrElse(
                analyzeFileOutput -> compileProjectResult.mapThrowingOrElse(
                    compileProjectOutput -> CommandFeedback.of(ShowFeedback.showText(evaluateTest(strategoRuntime, analyzeFileOutput, compileProjectOutput), "Test result for '" + file + "'")),
                    e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
                ),
                e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
            );
        } catch(InterruptedException | RuntimeException | Error e) {
            throw e; // Do not catch interrupted, runtime, and error exceptions.
        } catch(Exception e) {
            return CommandFeedback.ofTryExtractMessagesFrom(e, file);
        }
    }

    private String evaluateTest(
        StrategoRuntime strategoRuntime,
        ConstraintAnalyzeFile.Output analyzeFileOutput,
        ListView<StatixCompileModule.Output> compileProjectOutput
    ) throws Exception {
        final ITermFactory termFactory = strategoRuntime.getTermFactory();
        final ArrayList<IStrategoTerm> specTerms = compileProjectOutput.stream().map(o -> o.spec).collect(Collectors.toCollection(ArrayList::new));
        final IStrategoTerm result = strategoRuntime.addContextObject(analyzeFileOutput.context).invoke(
            "compile-test-body",
            analyzeFileOutput.ast,
            termFactory.makeList(specTerms)
        );
        if(result.getSubtermCount() < 2) {
            throw new InvalidAstShapeException("term with two subterms", result);
        }
        final StrategoTerms strategoTerms = new StrategoTerms(termFactory);
        final IStrategoTerm constraintTerm = result.getSubterm(0);
        final IStrategoTerm specTerm = result.getSubterm(1);
        final IStrategoTerm testResultTerm = evaluateTest(strategoTerms, constraintTerm, specTerm);
        final IStrategoTerm ppTestResultTerm = strategoRuntime.invoke("prettyprint-test-result", testResultTerm);
        return TermToString.toString(ppTestResultTerm);
    }


    private IStrategoTerm evaluateTest(StrategoTerms strategoTerms, IStrategoTerm constraintTerm, IStrategoTerm specTerm) throws InterruptedException {
        final IConstraint constraint = StatixTerms.constraint().match(strategoTerms.fromStratego(constraintTerm))
            .orElseThrow(() -> new InvalidAstShapeException("valid constraint term", constraintTerm));
        final Spec spec = StatixTerms.spec().match(strategoTerms.fromStratego(specTerm))
            .orElseThrow(() -> new InvalidAstShapeException("valid spec term", specTerm));
        final IDebugContext debug = new NullDebugContext();

        final SolverResult resultConfig = Solver.solve(spec, State.of(), constraint, debug, new NullCancel(), new NullProgress(), 0);
        // TODO: implement cancel

        final IUniDisunifier.Immutable unifier = resultConfig.state().unifier();

        final List<ITerm> substEntries = Lists.newArrayList();
        for(Map.Entry<ITermVar, ITermVar> e : resultConfig.existentials().entrySet()) {
            final ITerm v = StatixTerms.explode(e.getKey());
            final ITerm t = StatixTerms.explicateVars(unifier.findRecursive(e.getValue()));
            substEntries.add(B.newTuple(v, t));
        }

        final ITerm substTerm = B.newList(substEntries);
        final ITerm solverTerm = B.newBlob(resultConfig);
        final ITerm resultTerm = B.newAppl("Solution", substTerm, solverTerm);

        // TODO: At this point, It would be nice to call the stx--extract-messages strategy, to have the cascading error
        //       suppression. For now, I'll show a simple way to create messages

        final List<ITerm> errorList = Lists.newArrayList();
        final List<ITerm> warningList = Lists.newArrayList();
        final List<ITerm> noteList = Lists.newArrayList();

        // TODO: pass statix project config
        resultConfig.messages().forEach((c, m) -> MessageUtils.addMessage(m, c, unifier, IStatixProjectConfig.NULL, errorList, warningList, noteList));

        final IListTerm errors = B.newList(errorList);
        final IListTerm warnings = B.newList(warningList);
        final IListTerm notes = B.newList(noteList);

        return strategoTerms.toStratego(B.newAppl("EvalResult", resultTerm, errors, warnings, notes));
    }
}
