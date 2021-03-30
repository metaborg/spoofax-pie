package mb.spoofax.lwb.compiler;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.esv.CompileEsv;
import mb.spoofax.lwb.compiler.sdf3.CompileSdf3;
import mb.spoofax.lwb.compiler.statix.CompileStatix;
import mb.spoofax.lwb.compiler.stratego.CompileStratego;
import org.immutables.value.Value;

import javax.inject.Inject;

/**
 * Compiles a language specification by running the meta-language compilers.
 *
 * Takes as input a {@link ResourcePath} path to the root directory of the language specification.
 *
 * Produces a {@link Result} that is either an output with all {@link KeyedMessages messages} produced by the
 * meta-language compilers, or a {@link CompileLanguageSpecificationException} when compilation fails.
 */
@Value.Enclosing
public class CompileLanguageSpecification implements TaskDef<ResourcePath, Result<KeyedMessages, CompileLanguageSpecificationException>> {
    private final CompileSdf3 compileSdf3;
    private final CompileEsv compileEsv;
    private final CompileStatix compileStatix;
    private final CompileStratego compileStratego;

    @Inject public CompileLanguageSpecification(
        CompileSdf3 compileSdf3,
        CompileEsv compileEsv,
        CompileStatix compileStatix,
        CompileStratego compileStratego
    ) {
        this.compileSdf3 = compileSdf3;
        this.compileEsv = compileEsv;
        this.compileStatix = compileStatix;
        this.compileStratego = compileStratego;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, CompileLanguageSpecificationException> exec(ExecContext context, ResourcePath rootDirectory) {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final Result<KeyedMessages, CompileLanguageSpecificationException> result = context.require(compileSdf3, rootDirectory)
            .ifOk(messagesBuilder::addMessages)
            .mapErr(CompileLanguageSpecificationException::sdf3CompileFail)
            .and(
                context.require(compileEsv, rootDirectory)
                    .ifOk(messagesBuilder::addMessages)
                    .mapErr(CompileLanguageSpecificationException::esvCompileFail)
            ).and(
                context.require(compileStatix, rootDirectory)
                    .ifOk(messagesBuilder::addMessages)
                    .mapErr(CompileLanguageSpecificationException::statixCompileFail)
            ).and(
                context.require(compileStratego, rootDirectory)
                    .ifOk(messagesBuilder::addMessages)
                    .mapErr(CompileLanguageSpecificationException::strategoCompileFail)
            );
        if(result.isErr()) return result;
        return Result.ofOk(messagesBuilder.build());
    }
}
