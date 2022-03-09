package mb.spoofax.lwb.compiler.definition;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.dynamix.SpoofaxDynamixCompile;
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
 * Compiles the meta-langauge sources by running their compilers.
 *
 * Takes as input a {@link ResourcePath} path to the root directory of the meta-language sources.
 *
 * Produces a {@link Result} that is either an output with all {@link KeyedMessages messages} produced by the
 * meta-language compilers, or a {@link CompileMetaLanguageSourcesException} when compilation fails.
 */
@Value.Enclosing
public class CompileMetaLanguageSources implements TaskDef<ResourcePath, Result<CompileMetaLanguageSources.Output, CompileMetaLanguageSourcesException>> {
    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends CompileMetaLanguageSourcesData.Output.Builder {}

        static Builder builder() {return new Builder();}

        List<ResourcePath> providedJavaFiles();

        List<File> javaClassPaths();

        @Value.Default default KeyedMessages messages() {return KeyedMessages.of();}
    }


    private final SpoofaxSdf3Compile spoofaxSdf3Compile;
    private final SpoofaxEsvCompile spoofaxEsvCompile;
    private final SpoofaxStatixCompile spoofaxStatixCompile;
    private final SpoofaxDynamixCompile spoofaxDynamixCompile;
    private final SpoofaxStrategoCompile spoofaxStrategoCompile;

    @Inject public CompileMetaLanguageSources(
        SpoofaxSdf3Compile spoofaxSdf3Compile,
        SpoofaxEsvCompile spoofaxEsvCompile,
        SpoofaxStatixCompile spoofaxStatixCompile,
        SpoofaxDynamixCompile spoofaxDynamixCompile,
        SpoofaxStrategoCompile spoofaxStrategoCompile
    ) {
        this.spoofaxSdf3Compile = spoofaxSdf3Compile;
        this.spoofaxEsvCompile = spoofaxEsvCompile;
        this.spoofaxStatixCompile = spoofaxStatixCompile;
        this.spoofaxDynamixCompile = spoofaxDynamixCompile;
        this.spoofaxStrategoCompile = spoofaxStrategoCompile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Output, CompileMetaLanguageSourcesException> exec(ExecContext context, ResourcePath rootDirectory) {
        final ArrayList<ResourcePath> providedJavaFiles = new ArrayList<>();
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ArrayList<File> javaClassPaths = new ArrayList<>();
        final Result<?, CompileMetaLanguageSourcesException> result = context.require(spoofaxSdf3Compile, rootDirectory)
            .ifOk(messagesBuilder::addMessages)
            .mapErr(CompileMetaLanguageSourcesException::sdf3CompileFail)
            .and(
                context.require(spoofaxEsvCompile, rootDirectory)
                    .ifOk(messagesBuilder::addMessages)
                    .mapErr(CompileMetaLanguageSourcesException::esvCompileFail)
            ).and(
                context.require(spoofaxStatixCompile, rootDirectory)
                    .ifOk(messagesBuilder::addMessages)
                    .mapErr(CompileMetaLanguageSourcesException::statixCompileFail)
            ).and(
                context.require(spoofaxDynamixCompile, rootDirectory)
                    .ifOk(messagesBuilder::addMessages)
                    .mapErr(CompileMetaLanguageSourcesException::dynamixCompileFail)
            ).and(
                context.require(spoofaxStrategoCompile, rootDirectory)
                    .ifOk(o -> {
                        messagesBuilder.addMessages(o.messages());
                        providedJavaFiles.addAll(o.providedJavaFiles());
                        javaClassPaths.addAll(o.javaClassPaths());
                    })
                    .mapErr(CompileMetaLanguageSourcesException::strategoCompileFail)
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
