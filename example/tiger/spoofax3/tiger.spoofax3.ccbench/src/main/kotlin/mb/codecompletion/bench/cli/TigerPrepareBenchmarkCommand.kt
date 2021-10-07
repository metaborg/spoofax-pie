package mb.codecompletion.bench.cli

import mb.codecompletion.bench.TigerBenchmarkBuilder
import javax.inject.Inject

class TigerPrepareBenchmarkCommand @Inject constructor(
    benchmarkBuilder: TigerBenchmarkBuilder
) : PrepareBenchmarkCommand(
    benchmarkBuilder
)
