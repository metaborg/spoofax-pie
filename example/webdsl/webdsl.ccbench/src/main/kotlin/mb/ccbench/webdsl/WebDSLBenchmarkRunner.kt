package mb.ccbench.webdsl

import mb.ccbench.BenchmarkRunner
import mb.pie.api.Pie
import org.spoofax.interpreter.terms.ITermFactory
import javax.inject.Inject

class WebDSLBenchmarkRunner @Inject constructor(
    pie: Pie,
    runBenchmarkTask: WebDSLRunBenchmarkTask,
    termFactory: ITermFactory
) : BenchmarkRunner(
    pie,
    runBenchmarkTask,
    termFactory
)
