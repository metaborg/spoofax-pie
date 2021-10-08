package mb.ccbench.webdsl

import mb.ccbench.cli.PrepareBenchmarkCommand
import javax.inject.Inject

class WebDSLPrepareBenchmarkCommand @Inject constructor(
    benchmarkBuilder: WebDSLBenchmarkBuilder
) : PrepareBenchmarkCommand(
    benchmarkBuilder
)
