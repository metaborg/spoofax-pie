package mb.codecompletion.bench.cli

import mb.codecompletion.bench.TigerBenchmarkRunner
import javax.inject.Inject

class TigerRunBenchmarkCommand @Inject constructor(
    benchmarkRunner: TigerBenchmarkRunner
) : RunBenchmarkCommand(
    benchmarkRunner
)
