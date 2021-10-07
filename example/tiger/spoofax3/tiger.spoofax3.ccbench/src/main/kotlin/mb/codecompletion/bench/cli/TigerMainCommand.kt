package mb.codecompletion.bench.cli

import javax.inject.Inject

class TigerMainCommand @Inject constructor(
    prepareBenchmarkCommand: TigerPrepareBenchmarkCommand,
    runBenchmarkCommand: TigerRunBenchmarkCommand,
): MainCommand(
    prepareBenchmarkCommand,
    runBenchmarkCommand,
)
