package mb.ccbench.webdsl

import mb.ccbench.RunBenchmarkTask
import mb.webdsl.task.WebDSLParse
import mb.webdsl.task.WebDSLCodeCompletionTaskDef
import javax.inject.Inject

class WebDSLRunBenchmarkTask @Inject constructor(
    parseTask: WebDSLParse,
    codeCompletionTask: WebDSLCodeCompletionTaskDef
) : RunBenchmarkTask(
    parseTask,
    codeCompletionTask
)
