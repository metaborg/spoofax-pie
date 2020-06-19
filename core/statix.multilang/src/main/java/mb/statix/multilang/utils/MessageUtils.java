package mb.statix.multilang.utils;

import com.google.common.collect.Streams;
import mb.common.message.Message;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.jsglr.common.ResourceKeyAttachment;
import mb.jsglr.common.TermTracer;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.build.TermBuild;
import mb.nabl2.terms.stratego.StrategoAnnotations;
import mb.nabl2.terms.stratego.TermIndex;
import mb.nabl2.terms.stratego.TermOrigin;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.nabl2.util.TermFormatter;
import mb.resource.ResourceKey;
import mb.statix.constraints.Constraints;
import mb.statix.constraints.messages.IMessage;
import mb.statix.constraints.messages.MessageKind;
import mb.statix.solver.IConstraint;
import mb.statix.solver.persistent.Solver;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.functions.Function1;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.ITermAttachment;
import org.spoofax.terms.attachments.ParentAttachment;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageUtils {

    private static final int MAX_TRACE = 5;

    public static Message formatMessage(final IMessage message, final IConstraint constraint, final IUniDisunifier unifier) {
        final TermFormatter formatter = Solver.shallowTermFormatter(unifier);

        ITerm originTerm = message.origin().flatMap(t -> getOriginTerm(t, unifier)).orElse(null);
        final Deque<String> trace = new ArrayDeque<>();
        IConstraint current = constraint;
        int traceCount = 0;
        while(current != null) {
            if(originTerm == null) {
                originTerm = findOriginArgument(current, unifier).orElse(null);
            }
            if(traceCount++ < MAX_TRACE) {
                trace.addLast(current.toString(formatter));
            }
            current = current.cause().orElse(null);
        }
        if(traceCount >= MAX_TRACE) {
            trace.addLast("... trace truncated ...");
        }

        // use empty origin if none was found
        if(originTerm == null) {
            originTerm = TermBuild.B.EMPTY_TUPLE;
        }

        // add constraint message
        trace.addFirst(message.toString(formatter));

        final String messageText = trace.stream().filter(s -> !s.isEmpty()).map(MessageUtils::cleanupString)
            .collect(Collectors.joining("<br>\n&gt;&nbsp;"));

        return new Message(messageText, kindToSeverity(message.kind()), getRegion(originTerm));
    }

    private static Region getRegion(ITerm originTerm) {
        StrategoAnnotations strategoAnnotations = originTerm.getAttachments().getInstance(StrategoAnnotations.class);
        if(strategoAnnotations == null) {
            return null;
        }
        return strategoAnnotations.getAnnotationList()
            .stream()
            .filter(IStrategoAppl.class::isInstance)
            .map(IStrategoAppl.class::cast)
            .filter(appl -> appl.getConstructor().getName().equals("TermIndex"))
            .findAny()
            .map(TermTracer::getRegion)
            .orElse(null);
    }

    private static Severity kindToSeverity(MessageKind kind) {
        return kind == MessageKind.ERROR ? Severity.Error :
            kind == MessageKind.WARNING ? Severity.Warning :
                Severity.Info;
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
            onTellRel -> Stream.empty(),
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
            .map(t -> TermBuild.B.EMPTY_TUPLE.withAttachments(t.getAttachments()));
        // @formatter:on
    }

    private static String cleanupString(String string) {
        return string.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    public static @Nullable ResourceKey resourceKeyFromOrigin(ITerm origin) {
        if(origin.getAttachments().containsKey(TermOrigin.class)) {
            TermOrigin termOrigin = (TermOrigin)origin.getAttachments().get(TermOrigin.class);
            ITermAttachment parent = termOrigin.getImploderAttachment();
            return getResourceKeyFromParentAttachments(parent);
        }
        return null;
    }

    private static ResourceKey getResourceKeyFromParentAttachments(ITermAttachment attachment) {
        if(attachment == null) {
            return null;
        }
        if(attachment instanceof ResourceKeyAttachment) {
            return ((ResourceKeyAttachment)attachment).resourceKey;
        }

        if(attachment instanceof ParentAttachment) {
            IStrategoTerm parentTerm = ((ParentAttachment)attachment).getParent();
            ISimpleTerm root = ParentAttachment.getRoot(parentTerm);
            return getResourceKeyFromParentAttachments(root.getAttachment(ImploderAttachment.TYPE));
        }

        return getResourceKeyFromParentAttachments(attachment.getNext());
    }

    public static ResourceKey tryGetResourceKey(IConstraint constraint, IUniDisunifier unifier) {
        IConstraint current = constraint;

        while(current != null) {
            ITerm origin = findOriginArgument(current, unifier).orElse(null);
            if(origin != null) {
                ResourceKey result = resourceKeyFromOrigin(origin);
                if(result != null) {
                    return result;
                }
            }
            current = current.cause().orElse(null);
        }

        return null;
    }
}
