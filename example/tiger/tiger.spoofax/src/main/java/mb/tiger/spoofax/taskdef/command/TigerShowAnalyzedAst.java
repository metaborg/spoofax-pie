package mb.tiger.spoofax.taskdef.command;

import mb.common.region.Region;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.constraint.common.ConstraintAnalyzer;
import mb.jsglr.common.TermTracer;
import mb.pie.api.ExecContext;
import mb.pie.api.Provider;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedbacks;
import mb.spoofax.core.language.command.CommandOutput;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.spoofax.core.language.command.arg.TextToResourceKeyArgConverter;
import mb.stratego.common.StrategoUtil;
import mb.tiger.spoofax.taskdef.TigerAnalyze;
import mb.tiger.spoofax.taskdef.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;

public class TigerShowAnalyzedAst implements TaskDef<TigerShowArgs, CommandOutput>, CommandDef<TigerShowArgs> {
    private final TigerParse parse;
    private final TigerAnalyze analyze;
    private final TextToResourceKeyArgConverter textToResourceKeyArgConverter;


    @Inject
    public TigerShowAnalyzedAst(TigerParse parse, TigerAnalyze analyze, TextToResourceKeyArgConverter textToResourceKeyArgConverter) {
        this.parse = parse;
        this.analyze = analyze;
        this.textToResourceKeyArgConverter = textToResourceKeyArgConverter;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandOutput exec(ExecContext context, TigerShowArgs input) throws Exception {
        final ResourceKey key = input.key;
        final @Nullable Region region = input.region;

        final Provider<@Nullable IStrategoTerm> astProvider = parse.createAstProvider(key);
        final ConstraintAnalyzer.@Nullable SingleFileResult analysisResult = context.require(analyze, new TigerAnalyze.Input(key, astProvider));
        // noinspection ConstantConditions (analysisResult can really be null).
        if(analysisResult == null) {
            throw new RuntimeException("Cannot show analyzed AST, analysis result for '" + key + "' is null");
        }
        if(analysisResult.ast == null) {
            throw new RuntimeException("Cannot show analyzed AST, analyzed AST for '" + key + "' is null");
        }

        final IStrategoTerm term;
        if(region != null) {
            term = TermTracer.getSmallestTermEncompassingRegion(analysisResult.ast, region);
        } else {
            term = analysisResult.ast;
        }

        final String formatted = StrategoUtil.toString(term);
        return new CommandOutput(ListView.of(CommandFeedbacks.showText(formatted, "Analyzed AST for '" + key + "'", null)));
    }

    @Override public Task<CommandOutput> createTask(TigerShowArgs input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "Show analyzed AST";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous);
    }

    @Override public EnumSetView<CommandContextType> getRequiredContextTypes() {
        return EnumSetView.of(CommandContextType.Resource);
    }

    @Override public ParamDef getParamDef() {
        return TigerShowArgs.getParamDef();
    }

    @Override public TigerShowArgs fromRawArgs(RawArgs rawArgs) {
        return TigerShowArgs.fromRawArgs(rawArgs);
    }

    public CliCommand getCliCommandItem() {
        final String operation = "analyze";
        return CliCommand.of(operation, "Analyzes Tiger sources and shows the analyzed AST",
            CliCommand.of("file", "Analyzes given Tiger file and shows the analyzed AST", this, TigerShowArgs.getFileCliParams(operation)),
            CliCommand.of("text", "Analyzes given Tiger text and shows the analyzed AST", this, TigerShowArgs.getTextCliParams(operation, textToResourceKeyArgConverter))
        );
    }
}
