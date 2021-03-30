package mb.constraint.common;

import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.Term;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.util.TermUtils;

class MessageUtil {
    static void addMessagesFromTerm(
        KeyedMessagesBuilder messagesBuilder,
        IStrategoTerm messagesTerm,
        Severity severity,
        @Nullable ResourceKey resourceOverride,
        ResourceService resourceService,
        @Nullable ResourcePath rootDirectory
    ) {
        for(IStrategoTerm term : messagesTerm) {
            final IStrategoTerm node;
            final String text;
            if(term.getSubtermCount() == 2) {
                node = term.getSubterm(0);
                text = messageTermToString(term.getSubterm(1));
            } else {
                node = term;
                text = messageTermToString(term) + " (no tree node indicated)";
            }

            final @Nullable IStrategoTerm originNode = TermTracer.getOrigin(node);
            @Nullable ResourceKey resource;
            if(resourceOverride != null) { // Use resource override
                resource = resourceOverride;
            } else if(originNode != null) { // Use resource attachment from origin node
                resource = TermTracer.getResourceKey(originNode);
            } else { // Use resource attachment from node
                resource = TermTracer.getResourceKey(node);
            }
            if(resource == null && TermUtils.isAppl(node, "TermIndex", 2)) { // Use resource from TermIndex string
                final String resourceString = TermUtils.toJavaStringAt(node, 0);
                final ResourceKeyString resourceKeyString = ResourceKeyString.parse(resourceString);
                if(resourceKeyString.hasQualifier()) { // Absolute path with qualifier
                    resource = resourceService.getResourceKey(resourceKeyString);
                } else if(rootDirectory != null) { // No qualified -> relative path
                    resource = rootDirectory.appendAsRelativePath(resourceKeyString.getId());
                }
            }
            final @Nullable Region region = TermTracer.getRegion(node);
            messagesBuilder.addMessage(text, severity, resource, region);
        }
    }

    static void addAmbiguityWarnings(KeyedMessagesBuilder messagesBuilder, IStrategoTerm ast, @Nullable ResourceKey resourceOverride) {
        final TermVisitor termVisitor = new TermVisitor() {
            private @Nullable IStrategoTerm ambStart;

            @Override public void preVisit(@NonNull IStrategoTerm term) {
                if(ambStart == null && "amb".equals(Term.tryGetName(term))) {
                    final String text = "Term is ambiguous: " + ambToString(term);
                    final @Nullable Region region = TermTracer.getRegion(term);
                    if(region != null) {
                        final @Nullable ResourceKey resourceKey =
                            resourceOverride != null ? resourceOverride : TermTracer.getResourceKey(term);
                        messagesBuilder.addMessage(text, Severity.Warning, resourceKey, region);
                    } else {
                        messagesBuilder.addMessage(text, Severity.Warning);
                    }
                    ambStart = term;
                }
            }

            @Override public void postVisit(IStrategoTerm term) {
                if(term == ambStart) {
                    ambStart = null;
                }
            }

            private String ambToString(IStrategoTerm amb) {
                final String result = amb.toString();
                return result.length() > 5000 ? result.substring(0, 5000) + "..." : result;
            }
        };
        termVisitor.visit(ast);
    }


    static String messageTermToString(IStrategoTerm term) {
        if(term instanceof IStrategoString) {
            final IStrategoString messageStringTerm = (IStrategoString)term;
            return messageStringTerm.stringValue();
        } else if(term instanceof IStrategoList) {
            final StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(IStrategoTerm subterm : term) {
                if(!first) {
                    sb.append(' ');
                }
                sb.append(messageTermToString(subterm));
                first = false;
            }
            return sb.toString();
        } else {
            return term.toString();
        }
    }
}
