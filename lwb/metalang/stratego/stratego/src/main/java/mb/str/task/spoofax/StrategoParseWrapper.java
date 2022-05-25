package mb.str.task.spoofax;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.text.Text;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseInput;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.pie.JsglrParseTaskDef;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.str.StrategoClassLoaderResources;
import mb.str.StrategoParseTable;
import mb.str.StrategoParser;
import mb.str.StrategoParserFactory;
import mb.str.StrategoParserSelector;
import mb.str.StrategoScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

@StrategoScope
public class StrategoParseWrapper extends JsglrParseTaskDef {
    private final StrategoClassLoaderResources classLoaderResources;
    private final StrategoAnalyzeConfigFunctionWrapper configFunctionWrapper;
    private final StrategoParserSelector strategoParserSelector;

    @Inject public StrategoParseWrapper(
        StrategoClassLoaderResources classLoaderResources,
        StrategoAnalyzeConfigFunctionWrapper configFunctionWrapper,
        StrategoParserSelector strategoParserSelector
    ) {
        this.classLoaderResources = classLoaderResources;
        this.configFunctionWrapper = configFunctionWrapper;
        this.strategoParserSelector = strategoParserSelector;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<JsglrParseOutput, JsglrParseException> exec(ExecContext context, JsglrParseTaskInput input) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        // TODO: instead of requiring all origins for each file to parse, only require the origins that corresponds to a certain file.
        return Option.ofOptional(input.rootDirectoryHint()).mapThrowingOrElseThrowing(
            d -> configFunctionWrapper.get().apply(context, d).mapThrowingOrElse(
                o -> o.mapThrowingOrElseThrowing(
                    c -> {
                        c.sourceFileOrigins.forEach(sfo -> context.require(sfo, OutputStampers.inconsequential()));
                        return super.exec(context, input); // Parse normally after requiring source file origins.
                    },
                    () -> super.exec(context, input) // Stratego is not configured -> parse normally.
                ),
                e -> Result.ofErr(JsglrParseException.otherFail(e, Option.ofOptional(input.startSymbol()), input.fileHint(), Option.ofOptional(input.rootDirectoryHint()))) // Stratego configuration failed -> fail.
            ),
            () -> super.exec(context, input) // No directory hint is given, cannot get configuration -> parse normally.
        );
    }

    @Override
    protected Result<JsglrParseOutput, JsglrParseException> parse(
        ExecContext context,
        Text text,
        @Nullable String startSymbol,
        @Nullable ResourceKey fileHint,
        @Nullable ResourcePath rootDirectoryHint
    ) throws IOException, InterruptedException {
        // Copied from `StrategoParse`, but uses a `Provider<StrategoParser>` provided by `strategoParserSelector`.
        context.require(classLoaderResources.tryGetAsNativeDefinitionResource("target/metaborg/sdf.tbl"));
        context.require(classLoaderResources.tryGetAsNativeDefinitionResource("target/metaborg/table.bin"));
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsNativeResource(StrategoParser.class), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsNativeResource(StrategoParserFactory.class), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsNativeResource(StrategoParseTable.class), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsNativeResource(StrategoParserSelector.class), ResourceStampers.hashFile());
        final Result<Provider<StrategoParser>, ?> provider = strategoParserSelector.getParserProvider(context, fileHint, rootDirectoryHint);
        try {
            final StrategoParser parser = provider.unwrap().get();
            return Result.ofOk(parser.parse(new JsglrParseInput(text, startSymbol != null ? startSymbol : "Module", fileHint, rootDirectoryHint)));
        } catch(RuntimeException | InterruptedException e) {
            throw e;
        } catch(JsglrParseException e) {
            return Result.ofErr(e);
        } catch(Exception e) {
            return Result.ofErr(JsglrParseException.otherFail(e, Option.ofNullable(startSymbol), Option.ofNullable(fileHint), Option.ofNullable(rootDirectoryHint)));
        }
    }
}
