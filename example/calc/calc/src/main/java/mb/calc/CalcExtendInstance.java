package mb.calc;

import mb.calc.command.CalcShowAnalyzedAstCommand;
import mb.calc.command.CalcShowParsedAstCommand;
import mb.calc.command.CalcShowParsedTokensCommand;
import mb.calc.command.CalcShowPreAnalyzeAstCommand;
import mb.calc.command.CalcShowScopeGraphAstCommand;
import mb.calc.command.CalcShowScopeGraphCommand;
import mb.calc.command.CalcShowToJavaCommand;
import mb.calc.task.CalcAnalyze;
import mb.calc.task.CalcCheck;
import mb.calc.task.CalcCheckAggregator;
import mb.calc.task.CalcHover;
import mb.calc.task.CalcParse;
import mb.calc.task.CalcResolve;
import mb.calc.task.CalcStyle;
import mb.calc.task.CalcTestStrategoTaskDef;
import mb.calc.task.CalcTokenize;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.cli.CliParam;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;

import javax.inject.Inject;
import java.util.Set;

public class CalcExtendInstance extends CalcInstance {
    private final CalcShowToJavaCommand calcShowToJavaCommand;

    @Inject
    public CalcExtendInstance(
        CalcParse calcParse,
        CalcTokenize calcTokenize,
        CalcCheckAggregator calcCheckAggregator,
        CalcCheck calcCheck,
        CalcStyle calcStyle,
        CalcResolve calcResolve,
        CalcHover calcHover,
        CalcResourceExports calcResourceExports,
        CalcShowToJavaCommand calcShowToJavaCommand,
        CalcShowParsedAstCommand calcShowParsedAstCommand,
        CalcShowParsedTokensCommand calcShowParsedTokensCommand,
        CalcShowPreAnalyzeAstCommand calcShowPreAnalyzeAstCommand,
        CalcShowScopeGraphCommand calcShowScopeGraphCommand,
        CalcShowScopeGraphAstCommand calcShowScopeGraphAstCommand,
        CalcShowAnalyzedAstCommand calcShowAnalyzedAstCommand,
        CalcTestStrategoTaskDef calcTestStrategoTaskDef,
        CalcAnalyze calcAnalyze,
        Set<CommandDef<?>> commandDefs,
        Set<AutoCommandRequest<?>> autoCommandDefs
    ) {
        super(
            calcParse,
            calcTokenize,
            calcCheckAggregator,
            calcCheck,
            calcStyle,
            calcResolve,
            calcHover,
            calcResourceExports,
            calcShowToJavaCommand,
            calcShowParsedAstCommand,
            calcShowParsedTokensCommand,
            calcShowPreAnalyzeAstCommand,
            calcShowScopeGraphCommand,
            calcShowScopeGraphAstCommand,
            calcShowAnalyzedAstCommand,
            calcTestStrategoTaskDef,
            calcAnalyze,
            commandDefs,
            autoCommandDefs
        );
        this.calcShowToJavaCommand = calcShowToJavaCommand;
    }

    @Override public CliCommand getCliCommand() {
        return CliCommand.of(
            "Calc",
            CliCommand.of("to-java", "Convert Calc source files into Java source files", calcShowToJavaCommand, CliParam.positional("file", 0, "file", "Calc source file to convert to a Java source file"))
        );
    }
}
