package mb.spoofax.runtime.impl.nabl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.metaborg.meta.nabl2.config.NaBL2DebugConfig;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.constraints.messages.IMessageInfo;
import org.metaborg.meta.nabl2.constraints.messages.ImmutableMessageInfo;
import org.metaborg.meta.nabl2.constraints.messages.MessageContent;
import org.metaborg.meta.nabl2.constraints.messages.MessageKind;
import org.metaborg.meta.nabl2.solver.Fresh;
import org.metaborg.meta.nabl2.solver.ImmutablePartialSolution;
import org.metaborg.meta.nabl2.solver.PartialSolution;
import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.solver.Solver;
import org.metaborg.meta.nabl2.solver.SolverException;
import org.metaborg.meta.nabl2.spoofax.TermSimplifier;
import org.metaborg.meta.nabl2.spoofax.analysis.Actions;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult;
import org.metaborg.meta.nabl2.stratego.StrategoTerms;
import org.metaborg.meta.nabl2.stratego.TermOrigin;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.terms.ITermVar;
import org.metaborg.meta.nabl2.terms.generic.TB;
import org.metaborg.meta.nabl2.unification.IUnifier;
import org.metaborg.meta.nabl2.util.functions.Function1;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import mb.spoofax.runtime.impl.stratego.StrategoUtils;
import mb.spoofax.runtime.impl.util.MessageFormatter;
import mb.spoofax.runtime.impl.util.NullCancel;
import mb.spoofax.runtime.impl.util.NullProgress;
import mb.spoofax.runtime.model.SpoofaxEx;
import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.message.MsgConstants;
import mb.spoofax.runtime.model.message.MsgImpl;
import mb.spoofax.runtime.model.message.MsgSeverity;
import mb.spoofax.runtime.model.message.MsgType;
import mb.spoofax.runtime.model.message.PathMsg;
import mb.spoofax.runtime.model.message.PathMsgImpl;
import mb.spoofax.runtime.model.nats.NaTsMsgType;
import mb.spoofax.runtime.model.region.Region;
import mb.spoofax.runtime.model.region.RegionImpl;
import mb.vfs.path.PPath;
import mb.vfs.path.PathSrv;

public class ConstraintSolver {
    private static final MsgType messageType = new NaTsMsgType();
    static final String globalSource = "";

    private final PathSrv pathSrv;
    private final StrategoTerms strategoTerms;


    @Inject public ConstraintSolver(PathSrv pathSrv) {
        this.pathSrv = pathSrv;
        this.strategoTerms = new StrategoTerms(new ImploderOriginTermFactory(new TermFactory()));
    }


    public ImmutablePartialSolution solvePartial(InitialResult initialResult, UnitResult unitResult, PPath path)
        throws SpoofaxEx {
        final String source = path.toString();

        final List<ITerm> globalTerms = new ArrayList<>();
        Iterables.addAll(globalTerms, initialResult.getArgs().getParams());
        initialResult.getArgs().getType().ifPresent(globalTerms::add);

        final Fresh fresher = new Fresh();
        final Function1<String, ITermVar> fresh = base -> TB.newVar(source, fresher.fresh(base));
        final IMessageInfo messageInfo =
            ImmutableMessageInfo.of(MessageKind.ERROR, MessageContent.of(), Actions.sourceTerm(source));
        final NaBL2DebugConfig debugConfig = new NaBL2DebugConfig();
        final ImmutablePartialSolution partialSolution;
        try {
            partialSolution = (ImmutablePartialSolution) Solver.solveIncremental(initialResult.getConfig(), globalTerms,
                fresh, unitResult.getConstraints(), messageInfo, new NullProgress(), new NullCancel(), debugConfig);
        } catch(SolverException | InterruptedException e) {
            throw new SpoofaxEx(MessageFormatter.format("Failed to partially solve {}", source), e);
        }

        return partialSolution;
    }

    public ConstraintSolverSolution solve(InitialResult initialResult, Collection<? extends PartialSolution> partialSolutions,
        PPath projectPath) throws SpoofaxEx {
        final Set<IConstraint> constraints = initialResult.getConstraints();
        final Fresh fresher = new Fresh();
        final Function1<String, ITermVar> fresh = base -> TB.newVar(globalSource, fresher.fresh(base));
        final IMessageInfo messageInfo =
            ImmutableMessageInfo.of(MessageKind.ERROR, MessageContent.of(), Actions.sourceTerm(globalSource));
        final NaBL2DebugConfig debugConfig = new NaBL2DebugConfig();
        final Solution solution;
        try {
            solution = Solver.solveFinal(initialResult.getConfig(), fresh, constraints, partialSolutions, messageInfo,
                new NullProgress(), new NullCancel(), debugConfig);
        } catch(SolverException | InterruptedException e) {
            throw new SpoofaxEx("Failed to solve", e);
        }

        final ArrayList<PathMsg> fileMessages = new ArrayList<>();
        final ArrayList<Msg> projectMessages = new ArrayList<>();
        final ArrayList<Msg> solutionMessages =
            messages(solution.getMessages().getAll(), solution.getUnifier(), projectPath);
        for(Msg msg : solutionMessages) {
            if(msg instanceof PathMsg) {
                fileMessages.add((PathMsg) msg);
            } else {
                projectMessages.add(msg);
            }
        }

        final ArrayList<PathMsg> fileUnsolvedMessages = new ArrayList<>();
        final ArrayList<Msg> projectUnsolvedMessages = new ArrayList<>();
        final ArrayList<Msg> unsolvedMessages =
            messages(Solver.unsolvedErrors(solution.getUnsolvedConstraints()), solution.getUnifier(), projectPath);
        for(Msg msg : unsolvedMessages) {
            if(msg instanceof PathMsg) {
                fileUnsolvedMessages.add((PathMsg) msg);
            } else {
                projectUnsolvedMessages.add(msg);
            }
        }

        return new ConstraintSolverSolution(fileMessages, projectMessages, fileUnsolvedMessages,
            projectUnsolvedMessages);
    }

    private ArrayList<Msg> messages(Set<IMessageInfo> messages, IUnifier unifier, PPath projectPath) {
        return Iterables2.stream(messages).map(m -> message(m, unifier, projectPath))
            .collect(Collectors.toCollection(ArrayList::new));
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

    private Msg message(ITerm originatingTerm, IMessageInfo messageInfo, MsgSeverity severity, IUnifier unifier,
        PPath projectPath) {
        final Optional<TermOrigin> maybeOrigin = TermOrigin.get(originatingTerm);
        if(maybeOrigin.isPresent()) {
            final TermOrigin origin = maybeOrigin.get();
            final Region region = new RegionImpl(origin.getStartOffset(), origin.getEndOffset());
            final String pathStr = origin.getResource();
            final PPath path = pathSrv.resolveLocal(pathStr);
            final String relPath = projectPath.relativizeStringFrom(path);
            final String text = messageInfo.getContent().apply(unifier::find).toString(prettyPrint(relPath));
            return new PathMsgImpl(text, severity, messageType, region, null, path);
        } else {
            final String text = messageInfo.getContent().apply(unifier::find).toString(prettyPrint(null));
            return new MsgImpl(text, severity, messageType, null, null);
        }
    }

    private Function<ITerm, String> prettyPrint(@Nullable String relPath) {
        return term -> {
            final ITerm simpleTerm = TermSimplifier.focus(relPath, term);
            final IStrategoTerm strategoTerm = strategoTerms.toStratego(simpleTerm);
            final String text = StrategoUtils.toString(strategoTerm);
            return text;
        };
    }
}
