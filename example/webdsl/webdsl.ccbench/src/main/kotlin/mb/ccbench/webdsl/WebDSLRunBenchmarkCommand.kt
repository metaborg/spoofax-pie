package mb.ccbench.webdsl

import mb.ccbench.cli.RunBenchmarkCommand
import javax.inject.Inject

class WebDSLRunBenchmarkCommand @Inject constructor(
    benchmarkRunner: WebDSLBenchmarkRunner
) : RunBenchmarkCommand(
    benchmarkRunner
)
