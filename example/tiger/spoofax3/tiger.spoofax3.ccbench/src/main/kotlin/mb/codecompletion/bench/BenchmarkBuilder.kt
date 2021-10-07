package mb.codecompletion.bench

import mb.pie.api.Pie
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.inject.Inject
import kotlin.io.path.extension
import kotlin.streams.toList

/**
 * Builds a benchmark.
 */
class BenchmarkBuilder @Inject constructor(
    private val pie: Pie,
    private val prepareBenchmarkTask: PrepareBenchmarkTask,
) {

    /**
     * Builds a benchmark and saves it.
     *
     * @param name the name of the benchmark
     * @param projectDir the project directory
     * @param extension the language file extension, including the leading dot, (e.g., `*.tig`).
     * @param outputDir the output directory
     * @param testCaseDir the test case directory
     * @param sample how many tests to sample for each file
     * @param seed the sampling seed
     */
    fun build(
        name: String,
        projectDir: Path,
        extension: String,
        outputDir: Path,
        testCaseDir: Path,
        sample: Int?,
        seed: Long?
    ): Benchmark {
        // Gather all relevant files in the project directory,
        // and make their paths relative to the project directory
        val inputFiles = Files.walk(projectDir)
            .filter { ".${it.extension}" == extension }
            .map { projectDir.relativize(it) }
            .toList()

        val rnd = Random(seed ?: System.nanoTime())

        // Create and write the test cases
        val testCases = inputFiles.flatMap {
            println("Preparing $it...")
            val result = prepareBenchmarkTask.run(pie, projectDir, it, testCaseDir, sample, rnd)
            println("Prepared $it.")
            result
        }

        // Create and write the Benchmark object
        println("Writing benchmark file...")
        val benchmarkFile = outputDir.resolve("$name.yml")
        val benchmark = Benchmark(
            name,
            benchmarkFile.parent.relativize(testCaseDir),
            testCases
        )
        Files.createDirectories(benchmarkFile.parent)
        Benchmark.write(benchmark, benchmarkFile)
        println("Wrote benchmark file to $benchmarkFile")
        return benchmark
    }

}
