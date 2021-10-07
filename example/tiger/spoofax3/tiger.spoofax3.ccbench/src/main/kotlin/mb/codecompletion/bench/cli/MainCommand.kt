package mb.codecompletion.bench.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import javax.inject.Inject

abstract class MainCommand(
    prepareBenchmarkCommand: PrepareBenchmarkCommand,
    runBenchmarkCommand: RunBenchmarkCommand,
): CliktCommand() {
    init {
        subcommands(
            prepareBenchmarkCommand,
            runBenchmarkCommand,
        )
    }

    override fun run() = Unit
}
