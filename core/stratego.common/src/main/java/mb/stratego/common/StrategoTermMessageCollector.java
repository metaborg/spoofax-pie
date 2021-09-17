package mb.stratego.common;

import mb.aterm.common.InvalidAstShapeException;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

public class StrategoTermMessageCollector {
    public static void addTermMessages(
        IStrategoTerm messagesTerm,
        @Nullable ResourcePath fallbackResource,
        KeyedMessagesBuilder messagesBuilder
    ) {
        if(messagesTerm.getSubtermCount() != 3) {
            throw new InvalidAstShapeException("messages term with three subterms (representing errors, warnings, and notes)", messagesTerm);
        }
        addTermMessages(messagesTerm.getSubterm(0), Severity.Error, fallbackResource, messagesBuilder);
        addTermMessages(messagesTerm.getSubterm(1), Severity.Warning, fallbackResource, messagesBuilder);
        addTermMessages(messagesTerm.getSubterm(2), Severity.Info, fallbackResource, messagesBuilder);
    }

    private static void addTermMessages(
        IStrategoTerm messagesListTerm,
        Severity severity,
        @Nullable ResourcePath fallbackResource,
        KeyedMessagesBuilder messagesBuilder
    ) {
        for(IStrategoTerm messageTerm : messagesListTerm) {
            if(messageTerm.getSubtermCount() != 2) {
                throw new InvalidAstShapeException("message term with two subterms (representing a term to get the location of the message, and the message text)", messageTerm);
            }
            final IStrategoTerm originTerm = messageTerm.getSubterm(0);
            final @Nullable Region originRegion = TermTracer.getRegion(originTerm);
            final @Nullable ResourceKey originResource = TermTracer.getResourceKey(originTerm);
            final @Nullable ResourceKey resource = originResource != null ? originResource : fallbackResource;
            final String text = TermUtils.asJavaStringAt(messageTerm, 1)
                .orElseThrow(() -> new InvalidAstShapeException("message text string as second subterm", messageTerm));
            messagesBuilder.addMessage(text, severity, resource, originRegion);
        }
    }
}
