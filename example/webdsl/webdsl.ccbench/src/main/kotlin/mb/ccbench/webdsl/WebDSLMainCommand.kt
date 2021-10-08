package mb.ccbench.webdsl

import mb.ccbench.cli.MainCommand
import javax.inject.Inject

class WebDSLMainCommand @Inject constructor(
    prepareBenchmarkCommand: WebDSLPrepareBenchmarkCommand,
    runBenchmarkCommand: WebDSLRunBenchmarkCommand,
): MainCommand(
    prepareBenchmarkCommand,
    runBenchmarkCommand,
)
