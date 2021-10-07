package mb.codecompletion.bench.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import mb.codecompletion.bench.Benchmark
import mb.codecompletion.bench.BenchmarkRunner
import mb.codecompletion.bench.utils.withExtension
import java.nio.file.Files
import java.nio.file.Path

/**
 * Command that runs tests.
 */
abstract class RunBenchmarkCommand(
    private val benchmarkRunner: BenchmarkRunner,
) : CliktCommand(name = "run") {
    val name: String by argument(help = "Name of the benchmark")

    val projectDir: Path by option("-p", "--project", help = "Project directory").path(mustExist = true, canBeFile = false, canBeDir = true).required()
    val inputFile: Path? by option("-i", "--input", help = "Benchmark YAML file").path(mustExist = true, canBeFile = true, canBeDir = false)
    val outputDir: Path? by option("-o", "--output", help = "Output directory").path(mustExist = false, canBeFile = false, canBeDir = true)
    val samples: Int? by option("-s", "--sample", help = "How many samples in total").int()
    val seed: Long? by option("--seed", help = "The seed").long()

    override fun run() {
        val actualProjectDir = projectDir.toAbsolutePath()
        val actualInputFile = (inputFile ?: Path.of("$name.yml")).toAbsolutePath()
        val actualOutputDir = (outputDir ?: Path.of("output/")).toAbsolutePath()
        val actualOutputFile = actualOutputDir.resolve(actualInputFile.withExtension(".csv").fileName).toAbsolutePath()
        val tmpProjectDir = actualOutputDir.resolve("tmp-project/").toAbsolutePath()
        println("Project: $actualProjectDir")
        println("Input file: $actualInputFile")
        println("Output: $actualOutputDir")
        println("Output file: $actualOutputFile")
        println("Temp project dir: $tmpProjectDir")

        val benchmark = Benchmark.read(actualInputFile)
        val results = benchmarkRunner.run(
            benchmark,
            actualInputFile,
            actualProjectDir,
            tmpProjectDir,
            samples,
            seed,
        )
        Files.createDirectories(actualOutputFile.parent)
        results.writeAsCsv(actualOutputFile)
        println("Done!")
    }
}
