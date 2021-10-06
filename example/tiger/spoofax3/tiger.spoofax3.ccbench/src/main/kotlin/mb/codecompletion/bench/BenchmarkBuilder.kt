package mb.codecompletion.bench

import mb.pie.api.Pie
import java.nio.file.Files
import java.nio.file.Path
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
     * @param baseDir the base directory
     * @param projectDir the project directory, either absolute or relative to the base directory
     * @param extension the language file extension, including the leading dot, (e.g., `*.tig`).
     * @param outputDir the output directory, either absolute or relative to the base directory
     * @param testCaseDir the test case directory, either absolute or relative to the output directory
     */
    fun build(
        name: String,
        baseDir: Path,
        projectDir: Path,
        extension: String,
        outputDir: Path,
        testCaseDir: Path,
    ): Benchmark {
        val resProjectDir = baseDir.resolve(projectDir)
        val resOutputDir = baseDir.resolve(outputDir)
        val resTestCaseDir = baseDir.resolve(testCaseDir)

        // Gather all relevant files in the project directory,
        // and make their paths relative to the project directory
        val inputFiles = Files.walk(resProjectDir)
            .filter { ".${it.extension}" == extension }
            .map { resProjectDir.relativize(it) }
            .toList()

        // Create and write the test cases
        val testCases = inputFiles.flatMap { prepareBenchmarkTask.run(pie, resProjectDir, it, resTestCaseDir) }

        // Create and write the Benchmark object
        val benchmark = Benchmark(
            name,
            projectDir,
            testCaseDir,
            testCases
        )
        val benchmarkFile = resOutputDir.resolve("$name.yml")
        Files.createDirectories(benchmarkFile.parent)
        Benchmark.write(benchmark, benchmarkFile)
        return benchmark
    }

}
