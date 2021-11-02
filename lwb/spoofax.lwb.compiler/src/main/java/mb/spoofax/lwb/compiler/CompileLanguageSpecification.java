package mb.spoofax.lwb.compiler;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvCompile;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Compile;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixCompile;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoCompile;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Compiles a language specification by running the meta-language compilers.
 *
 * Takes as input a {@link ResourcePath} path to the root directory of the language specification.
 *
 * Produces a {@link Result} that is either an output with all {@link KeyedMessages messages} produced by the
 * meta-language compilers, or a {@link CompileLanguageSpecificationException} when compilation fails.
 */
@Value.Enclosing
public class CompileLanguageSpecification implements TaskDef<ResourcePath, Result<CompileLanguageSpecification.Output, CompileLanguageSpecificationException>> {
    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends CompileLanguageSpecificationData.Output.Builder {}

        static Builder builder() { return new Builder(); }

        List<ResourcePath> providedJavaFiles();

        List<File> javaClassPaths();

        @Value.Default default KeyedMessages messages() { return KeyedMessages.of(); }
    }


    private final SpoofaxSdf3Compile spoofaxSdf3Compile;
    private final SpoofaxEsvCompile spoofaxEsvCompile;
    private final SpoofaxStatixCompile spoofaxStatixCompile;
    private final SpoofaxStrategoCompile spoofaxStrategoCompile;

    @Inject public CompileLanguageSpecification(
        SpoofaxSdf3Compile spoofaxSdf3Compile,
        SpoofaxEsvCompile spoofaxEsvCompile,
        SpoofaxStatixCompile spoofaxStatixCompile,
        SpoofaxStrategoCompile spoofaxStrategoCompile
    ) {
        this.spoofaxSdf3Compile = spoofaxSdf3Compile;
        this.spoofaxEsvCompile = spoofaxEsvCompile;
        this.spoofaxStatixCompile = spoofaxStatixCompile;
        this.spoofaxStrategoCompile = spoofaxStrategoCompile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Output, CompileLanguageSpecificationException> exec(ExecContext context, ResourcePath rootDirectory) {
        final ArrayList<ResourcePath> providedJavaFiles = new ArrayList<>();
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ArrayList<File> javaClassPaths = new ArrayList<>();
        final Result<?, CompileLanguageSpecificationException> result = context.require(spoofaxSdf3Compile, rootDirectory)
            .ifOk(messagesBuilder::addMessages)
            .mapErr(CompileLanguageSpecificationException::sdf3CompileFail)
            .and(
                context.require(spoofaxEsvCompile, rootDirectory)
                    .ifOk(messagesBuilder::addMessages)
                    .mapErr(CompileLanguageSpecificationException::esvCompileFail)
            ).and(
                context.require(spoofaxStatixCompile, rootDirectory)
                    .ifOk(messagesBuilder::addMessages)
                    .mapErr(CompileLanguageSpecificationException::statixCompileFail)
            ).and(
                context.require(spoofaxStrategoCompile, rootDirectory)
                    .ifOk(o -> {
                        messagesBuilder.addMessages(o.messages());
                        providedJavaFiles.addAll(o.providedJavaFiles());
                        javaClassPaths.addAll(o.javaClassPaths());
                    })
                    .mapErr(CompileLanguageSpecificationException::strategoCompileFail)
            );
        if(result.isErr()) return result.ignoreValueIfErr();
        return Result.ofOk(Output.builder()
            .providedJavaFiles(providedJavaFiles)
            .javaClassPaths(javaClassPaths)
            .messages(messagesBuilder.build())
            .build()
        );
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
