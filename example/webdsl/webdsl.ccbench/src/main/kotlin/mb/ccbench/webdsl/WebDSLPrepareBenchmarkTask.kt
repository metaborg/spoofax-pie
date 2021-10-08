package mb.ccbench.webdsl

import mb.ccbench.PrepareBenchmarkTask
import mb.common.util.ListView
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef
import mb.pie.api.TaskDef
import mb.resource.hierarchical.ResourcePath
import mb.resource.text.TextResourceRegistry
import mb.webdsl.task.WebDSLAnalyze
import mb.webdsl.task.WebDSLAnalyzeMulti
import mb.webdsl.task.WebDSLParse
import mb.webdsl.task.WebDSLPostAnalyzeStatix
import mb.webdsl.task.WebDSLPreAnalyzeStatix
import mb.webdsl.task.WebDSLUpgradePlaceholdersStatix
import mb.webdsl.task.WebDSLDowngradePlaceholdersStatix
import mb.webdsl.task.WebDSLGetSourceFiles
import mb.webdsl.task.WebDSLPPPartial
import org.spoofax.interpreter.terms.ITermFactory
import org.spoofax.terms.io.SimpleTextTermWriter
import javax.inject.Inject

class WebDSLPrepareBenchmarkTask @Inject constructor(
    parseTask: WebDSLParse,
    getSourceFilesTask: WebDSLGetSourceFiles,
    analyzeTask: WebDSLAnalyze,
//    analyzeTask: WebDSLAnalyzeMulti,
    explicateTask: WebDSLPreAnalyzeStatix,
    implicateTask: WebDSLPostAnalyzeStatix,
    upgradePlaceholdersTask: WebDSLUpgradePlaceholdersStatix,
    downgradePlaceholdersTask: WebDSLDowngradePlaceholdersStatix,
    prettyPrintTask: WebDSLPPPartial,
    textResourceRegistry: TextResourceRegistry,
    termFactory: ITermFactory,
    termWriter: SimpleTextTermWriter
) : PrepareBenchmarkTask(
    parseTask,
    getSourceFilesTask,
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
