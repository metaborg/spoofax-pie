package mb.statix.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.TermIndex;
import mb.nabl2.terms.stratego.TermOrigin;
import mb.nabl2.terms.substitution.ISubstitution;
import mb.nabl2.terms.substitution.PersistentSubstitution;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.nabl2.util.TermFormatter;
import mb.statix.constraints.Constraints;
import mb.statix.constraints.messages.IMessage;
import mb.statix.solver.IConstraint;
import mb.statix.solver.persistent.Solver;
import mb.statix.spoofax.IStatixProjectConfig;
import org.metaborg.util.functions.Function1;
import org.metaborg.util.tuple.Tuple2;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mb.nabl2.terms.build.TermBuild.B;

public class MessageUtils {
    ////////////////////////////////////////////////
    // Helper methods for creating error messages //
    ////////////////////////////////////////////////

    public static void addMessage(final IMessage message, final IConstraint constraint, final IUniDisunifier unifier,
                              IStatixProjectConfig config, final Collection<ITerm> errors, final Collection<ITerm> warnings,
                              final Collection<ITerm> notes) {
        Tuple2<Iterable<String>, ITerm> message_origin = formatMessage(message, constraint, unifier, config);

        final String messageText = Streams.stream(message_origin._1()).filter(s -> !s.isEmpty())
            .map(s -> cleanupString(s)).collect(Collectors.joining("<br>\n&gt;&nbsp;", "", "<br>\n"));

        final ITerm messageTerm = B.newTuple(message_origin._2(), B.newString(messageText));
        switch(message.kind()) {
            case ERROR:
                errors.add(messageTerm);
                break;
            case WARNING:
                warnings.add(messageTerm);
                break;
            case NOTE:
                notes.add(messageTerm);
                break;
            case IGNORE:
                break;
        }

    }

    public static Tuple2<Iterable<String>, ITerm> formatMessage(final IMessage message, final IConstraint constraint,
                                                                final IUniDisunifier unifier, IStatixProjectConfig config) {
        final TermFormatter formatter = Solver.shallowTermFormatter(unifier,
            config.messageTermDepth(config.messageTermDepth(IStatixProjectConfig.DEFAULT_MESSAGE_TERM_DEPTH)));
        final int maxTraceLength =
            config.messageTraceLength(config.messageTraceLength(IStatixProjectConfig.DEFAULT_MESSAGE_TRACE_LENGTH));

        ITerm originTerm = message.origin().flatMap(t -> getOriginTerm(t, unifier)).orElse(null);
        final Deque<String> trace = new LinkedList<>();
        IConstraint current = constraint;
        int traceCount = 0;
        while(current != null) {
            if(originTerm == null) {
                originTerm = findOriginArgument(current, unifier).orElse(null);
            }
            if(maxTraceLength < 0 || ++traceCount <= maxTraceLength) {
                trace.addLast(current.toString(formatter));
            }
            current = current.cause().orElse(null);
        }
        if(maxTraceLength > 0 && traceCount > maxTraceLength) {
            trace.addLast("... trace truncated ...");
        }

        // add constraint message
        trace.addFirst(message.toString(formatter, () -> constraint.toString(formatter), completeness -> {
            final ISubstitution.Transient subst = PersistentSubstitution.Transient.of();
            completeness.vars().forEach(var -> {
                ITerm sub = unifier.findRecursive(var);
                if(!sub.equals(var)) {
                    subst.put(var, sub);
                }
            });
            return completeness.apply(subst.freeze()).entrySet().stream().flatMap(e -> {
                String scope = e.getKey().toString();
                return e.getValue().elementSet().stream().map(edge -> scope + "-" + edge.toString());
            }).collect(Collectors.joining(", "));
        }));

        // use empty origin if none was found
        if(originTerm == null) {
            originTerm = B.newTuple();
        }

        return Tuple2.of(trace, originTerm);
    }

    private static Optional<ITerm> findOriginArgument(IConstraint constraint, IUniDisunifier unifier) {
        // @formatter:off
        final Function1<IConstraint, Stream<ITerm>> terms = Constraints.cases(
            onArith -> Stream.empty(),
            onConj -> Stream.empty(),
            onEqual -> Stream.empty(),
            onExists -> Stream.empty(),
            onFalse -> Stream.empty(),
            onInequal -> Stream.empty(),
            onNew -> Stream.empty(),
            onResolveQuery -> Stream.empty(),
            onTellEdge -> Stream.empty(),
            onTermId -> Stream.empty(),
            onTermProperty -> Stream.empty(),
            onTrue -> Stream.empty(),
            onTry -> Stream.empty(),
            onUser -> onUser.args().stream()
        );
        return terms.apply(constraint)
            .flatMap(t -> Streams.stream(getOriginTerm(t, unifier)))
            .findFirst();
        // @formatter:on
    }

    private static Optional<ITerm> getOriginTerm(ITerm term, IUniDisunifier unifier) {
        // @formatter:off
        return Optional.of(unifier.findTerm(term))
            .filter(t -> TermIndex.get(t).isPresent())
            .filter(t -> TermOrigin.get(t).isPresent()) // HACK Ignore terms without origin, such as empty lists
            .map(t -> B.newTuple(ImmutableList.of(), t.getAttachments()));
        // @formatter:on
    }

    private static String cleanupString(String string) {
        return string.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

