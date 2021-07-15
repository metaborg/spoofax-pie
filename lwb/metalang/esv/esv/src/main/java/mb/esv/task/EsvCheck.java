package mb.esv.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.option.Option;
import mb.esv.EsvClassLoaderResources;
import mb.esv.EsvScope;
import mb.esv.task.spoofax.EsvParseWrapper;
import mb.esv.util.EsvVisitor;
import mb.jsglr.common.TermTracer;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

@EsvScope
public class EsvCheck implements TaskDef<EsvConfig, KeyedMessages> {
    private final EsvClassLoaderResources classLoaderResources;
    private final EsvParseWrapper parse;

    @Inject public EsvCheck(EsvClassLoaderResources classLoaderResources, EsvParseWrapper parse) {
        this.classLoaderResources = classLoaderResources;
        this.parse = parse;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, EsvConfig config) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final EsvVisitor visitor = new EsvVisitor(parse, config.includeDirectorySuppliers, config.includeAstSuppliers) {
            @Override
            protected void acceptIncludeDirectorySupplyFail(IStrategoTerm importTerm, String importName, Exception e) {
                addMessageWithTerm("Failed to supply include directory for import " + importName + "; skipping", e, Severity.Warning, importTerm);
            }

            @Override
            protected void acceptIncludeAstSupplyFail(IStrategoTerm importTerm, String importName, Exception e) {
                addMessageWithTerm("Failed to supply include AST for import " + importName + "; skipping", e, Severity.Warning, importTerm);
            }

            @Override
            protected void acceptUnresolvedImport(IStrategoTerm importTerm, String importName) {
                addMessageWithTerm("Failed to resolve import " + importName, null, Severity.Error, importTerm);
            }

            @Override
            protected void acceptParseFail(JsglrParseException parseException) {
                final Option<ResourceKey> resource = parseException.getFileHint().or(parseException.getRootDirectoryHint().map(p -> p));
                resource.ifElse(
                    r -> messagesBuilder.extractMessagesRecursivelyWithFallbackKey(parseException, r),
                    () -> messagesBuilder.extractMessagesRecursively(parseException)
                );
            }

            @Override
            protected void acceptParse(JsglrParseOutput parseOutput) {
                messagesBuilder.addMessages(parseOutput.messages);
            }

            private void addMessageWithTerm(String text, @Nullable Throwable e, Severity severity, IStrategoTerm term) {
                messagesBuilder.addMessage(text, e, severity, TermTracer.getResourceKey(term), TermTracer.getRegion(term));
            }
        };
        visitor.visitMainFile(context, config.mainFile, config.rootDirectory);
        return messagesBuilder.build();
    }
}
