package mb.codecompletion.bench

import mb.pie.api.Pie
import javax.inject.Inject

class TigerBenchmarkBuilder @Inject constructor(
    pie: Pie,
    prepareBenchmarkTask: TigerPrepareBenchmarkTask
) : BenchmarkBuilder(
    pie,
    prepareBenchmarkTask
)
