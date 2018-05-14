package mb.spoofax.runtime.impl.nabl;

import com.google.inject.Inject;
import mb.nabl2.config.NaBL2DebugConfig;
import mb.nabl2.constraints.messages.*;
import mb.nabl2.scopegraph.terms.Scope;
import mb.nabl2.solver.*;
import mb.nabl2.solver.messages.Messages;
import mb.nabl2.solver.solvers.BaseSolver.BaseSolution;
import mb.nabl2.solver.solvers.BaseSolver.GraphSolution;
import mb.nabl2.solver.solvers.*;
import mb.nabl2.spoofax.TermSimplifier;
import mb.nabl2.spoofax.analysis.*;
import mb.nabl2.stratego.ConstraintTerms;
import mb.nabl2.stratego.TermOrigin;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.unification.IUnifier;
import mb.nabl2.terms.unification.PersistentUnifier;
import mb.spoofax.runtime.impl.util.*;
import mb.spoofax.runtime.model.SpoofaxEx;
import mb.spoofax.runtime.model.message.*;
import mb.spoofax.runtime.model.nats.NaTsMsgType;
import mb.spoofax.runtime.model.region.Region;
import mb.spoofax.runtime.model.region.RegionImpl;
import mb.vfs.path.PPath;
import mb.vfs.path.PathSrv;
import org.metaborg.util.functions.Function1;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConstraintSolver {
    private static final MsgType messageType = new NaTsMsgType();
    static final String globalSource = "";

    private final PathSrv pathSrv;


    @Inject public ConstraintSolver(PathSrv pathSrv) {
        this.pathSrv = pathSrv;
    }


    public ImmutableSolution solveGlobal(InitialResult globalConstraints) throws SpoofaxEx {
        final Fresh globalFresher = new Fresh();
        final Function1<String, String> globalFresh = globalFresher::fresh;
        final SemiIncrementalMultiFileSolver solver = new SemiIncrementalMultiFileSolver(new NaBL2DebugConfig(), dummyCallExternal());
        try {
            final BaseSolution baseSolution = ImmutableBaseSolution.of(globalConstraints.getConfig(),
                globalConstraints.getConstraints(), PersistentUnifier.Immutable.of());
            GraphSolution preSolution = solver.solveGraph(baseSolution, globalFresh, dummyCancel(), dummyProgress());
            preSolution = solver.reportUnsolvedGraphConstraints(preSolution);
            final HashSet<ITermVar> interfaceVars = interfaceVars(globalConstraints);
            ISolution initialSolution = solver.solveIntra(preSolution, interfaceVars, null, globalFresh, dummyCancel(), dummyProgress());
            return (ImmutableSolution) initialSolution;
        } catch(SolverException e) {
            throw new SpoofaxEx("Failed to initially solve", e);
        } catch(InterruptedException e) {
            // Cannot occur because dummy cancel is used; ignore with RuntimeException
            throw new RuntimeException("Initial solve cancelled", e);
        }
    }

    public ImmutableSolution solveDocument(UnitResult documentConstraints, InitialResult globalConstraints, ISolution globalSolution) throws SpoofaxEx {
        final Fresh fresher = new Fresh();
        final Function1<String, String> fresh = fresher::fresh;
        final SemiIncrementalMultiFileSolver solver = new SemiIncrementalMultiFileSolver(new NaBL2DebugConfig(), dummyCallExternal());
        try {
            final BaseSolution baseSolution =
                ImmutableBaseSolution.of(globalConstraints.getConfig(), documentConstraints.getConstraints(), globalSolution.unifier());
            GraphSolution preSolution = solver.solveGraph(baseSolution, fresh, dummyCancel(), dummyProgress());
            preSolution = solver.reportUnsolvedGraphConstraints(preSolution);
            final HashSet<ITermVar> interfaceVars = interfaceVars(globalConstraints);
            final HashSet<Scope> interfaceScopes = interfaceScopes(globalConstraints, globalSolution);
            ISolution unitSolution = solver.solveIntra(preSolution, interfaceVars, interfaceScopes, fresh, dummyCancel(), dummyProgress());
            return (ImmutableSolution) unitSolution;
        } catch(SolverException e) {
            throw new SpoofaxEx(MessageFormatter.format("Failed to partially solve {}", documentConstraints), e);
        } catch(InterruptedException e) {
            // Cannot occur because dummy cancel is used; ignore with RuntimeException
            throw new RuntimeException("Partial solve cancelled", e);
        }
    }

    public ConstraintSolverSolution solve(Collection<? extends ISolution> documentSolutions, ISolution globalSolution, PPath projectPath) throws SpoofaxEx {
        final Fresh globalFresher = new Fresh();
        final Function1<String, String> globalFresh = globalFresher::fresh;

        final SemiIncrementalMultiFileSolver solver = new SemiIncrementalMultiFileSolver(new NaBL2DebugConfig(), dummyCallExternal());
        ISolution solution;
        try {
            final IMessageInfo message = ImmutableMessageInfo.of(MessageKind.ERROR, MessageContent.of(), Actions.sourceTerm(globalSource));
            solution = solver.solveInter(globalSolution, documentSolutions, message, globalFresh, dummyCancel(), dummyProgress());
            solution = solver.reportUnsolvedConstraints(solution);
        } catch(SolverException e) {
            throw new SpoofaxEx(MessageFormatter.format("Failed to finally solve"), e);
        } catch(InterruptedException e) {
            // Cannot occur because dummy cancel is used; ignore with RuntimeException
            throw new RuntimeException("Partial solve cancelled", e);
        }

        final Messages.Transient messageBuilder = Messages.Transient.of();
        messageBuilder.addAll(Messages.unsolvedErrors(solution.constraints()));
        messageBuilder.addAll(solution.messages().getAll());
        final ArrayList<Msg> messages = messages(messageBuilder.freeze().getAll(), solution.unifier(), projectPath);
        final ArrayList<PathMsg> fileMessages = new ArrayList<>();
        final ArrayList<Msg> projectMessages = new ArrayList<>();
        for(Msg messageInfo : messages) {
            if(messageInfo instanceof PathMsg) {
                fileMessages.add((PathMsg) messageInfo);
            } else {
                projectMessages.add(messageInfo);
            }
        }
        return new ConstraintSolverSolution(fileMessages, projectMessages);
    }


    private HashSet<ITermVar> interfaceVars(InitialResult initialResult) {
        final HashSet<ITermVar> interfaceVars = new HashSet<>();
        initialResult.getArgs().getParams().forEach(param -> interfaceVars.addAll(param.getVars()));
        initialResult.getArgs().getType().ifPresent(type -> interfaceVars.addAll(type.getVars()));
        return interfaceVars;
    }

    private HashSet<Scope> interfaceScopes(InitialResult initialResult, ISolution initialSolution) {
        final HashSet<Scope> interfaceScopes = new HashSet<>();
        initialResult.getArgs().getParams().forEach(
            param -> Scope.matcher().match(param, initialSolution.unifier()).ifPresent(interfaceScopes::add));
        return interfaceScopes;
    }


    private CallExternal dummyCallExternal() {
        return (name, args) -> Optional.empty();
    }

    private ICancel dummyCancel() {
        return new NullCancel();
    }

    private IProgress dummyProgress() {
        return new NullProgress();
    }


    protected ArrayList<Msg> messages(Collection<IMessageInfo> messages, IUnifier unifier, PPath projectPath) {
        return messages.stream().map(m -> message(m, unifier, projectPath)).collect(Collectors.toCollection(ArrayList::new));
    }

    private Msg message(IMessageInfo message, IUnifier unifier, PPath projectPath) {
        final MsgSeverity severity;
        switch(message.getKind()) {
            default:
            case ERROR:
                severity = MsgConstants.errorSeverity;
                break;
            case WARNING:
                severity = MsgConstants.warningSeverity;
                break;
            case NOTE:
                severity = MsgConstants.infoSeverity;
                break;
        }
        return message(message.getOriginTerm(), message, severity, unifier, projectPath);
    }

    private Msg message(ITerm originatingTerm, IMessageInfo messageInfo, MsgSeverity severity, IUnifier unifier, PPath projectPath) {
        Optional<TermOrigin> maybeOrigin = TermOrigin.get(originatingTerm);
        if(maybeOrigin.isPresent()) {
            TermOrigin origin = maybeOrigin.get();
            final Region region = new RegionImpl(origin.getLeftToken().getStartOffset(), origin.getRightToken().getEndOffset());
            final String pathStr = origin.getResource();
            final PPath path = pathSrv.resolveLocal(pathStr);
            final String relPath = projectPath.relativizeStringFrom(path);
            String text = messageInfo.getContent().apply(unifier::findRecursive).toString(prettyprint(relPath));
            return new PathMsgImpl(text, severity, messageType, region, null, path);
        } else {
            String text =
                messageInfo.getContent().apply(unifier::findRecursive).toString(prettyprint(null));
            return new MsgImpl(text, severity, messageType, null, null);
        }
    }

    private Function<ITerm, String> prettyprint(@Nullable String source) {
        return term -> {
            final ITerm simpleTerm = ConstraintTerms.explicate(TermSimplifier.focus(source, term));
            String text = simpleTerm.toString();
            return text;
        };
    }
}
