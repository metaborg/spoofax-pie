package mb.ccbench.webdsl

import mb.ccbench.BenchmarkBuilder
import mb.pie.api.Pie
import javax.inject.Inject

class WebDSLBenchmarkBuilder @Inject constructor(
    pie: Pie,
    prepareBenchmarkTask: WebDSLPrepareBenchmarkTask
) : BenchmarkBuilder(
    pie,
    prepareBenchmarkTask
)
