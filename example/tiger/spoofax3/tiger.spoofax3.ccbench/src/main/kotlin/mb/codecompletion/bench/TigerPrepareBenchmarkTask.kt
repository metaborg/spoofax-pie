package mb.codecompletion.bench

import mb.resource.text.TextResourceRegistry
import mb.tiger.task.TigerAnalyze
import mb.tiger.task.TigerDowngradePlaceholdersStatix
import mb.tiger.task.TigerPPPartial
import mb.tiger.task.TigerParse
import mb.tiger.task.TigerPostAnalyzeStatix
import mb.tiger.task.TigerPreAnalyzeStatix
import mb.tiger.task.TigerUpgradePlaceholdersStatix
import org.spoofax.interpreter.terms.ITermFactory
import org.spoofax.terms.io.SimpleTextTermWriter
import javax.inject.Inject

class TigerPrepareBenchmarkTask @Inject constructor(
    parseTask: TigerParse,
    analyzeTask: TigerAnalyze,
    explicateTask: TigerPreAnalyzeStatix,
    implicateTask: TigerPostAnalyzeStatix,
    upgradePlaceholdersTask: TigerUpgradePlaceholdersStatix,
    downgradePlaceholdersTask: TigerDowngradePlaceholdersStatix,
    prettyPrintTask: TigerPPPartial,
    textResourceRegistry: TextResourceRegistry,
    termFactory: ITermFactory,
    termWriter: SimpleTextTermWriter
) : PrepareBenchmarkTask(
    parseTask,
    analyzeTask,
    explicateTask,
    implicateTask,
    upgradePlaceholdersTask,
    downgradePlaceholdersTask,
    prettyPrintTask,
    textResourceRegistry,
    termFactory,
    termWriter
)
