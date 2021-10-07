package mb.codecompletion.bench.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import mb.codecompletion.bench.BenchmarkBuilder
import java.nio.file.Path

/**
 * Command that prepares tests.
 */
abstract class PrepareBenchmarkCommand(
    private val benchmarkBuilder: BenchmarkBuilder,
) : CliktCommand(name = "prepare") {
    val name: String by argument(help = "Name of the benchmark")
    val ext: String by option("-e", "--ext", help = "File extension").required()

    val projectDir: Path by option("-p", "--project", help = "Project directory").path(mustExist = true, canBeFile = false, canBeDir = true).required()
    val outputDir: Path? by option("-o", "--output", help = "Output directory").path(mustExist = false, canBeFile = false, canBeDir = true)
    val testCaseDir: Path? by option("-t", "--testcaseDir", help = "Test case directory").path(mustExist = false, canBeFile = false, canBeDir = true)
    val samples: Int? by option("-s", "--sample", help = "How many to sample per file").int()
    val seed: Long? by option("--seed", help = "The seed").long()

    override fun run() {
        val actualProjectDir = projectDir.toAbsolutePath()
        val actualOutputDir = (outputDir ?: Path.of("output/")).toAbsolutePath()
        val actualTestCaseDir = (testCaseDir ?: actualOutputDir).toAbsolutePath()
        println("Project: $actualProjectDir")
        println("Output: $actualOutputDir")
        println("Testcases: $actualTestCaseDir")
        benchmarkBuilder.build(
            name,
            actualProjectDir,
            ".$ext",
            actualOutputDir,
            actualTestCaseDir,
            samples,
            seed,
        )
        println("Done!")
    }
}
