package mb.codecompletion.bench

import mb.codecompletion.bench.utils.sample
import mb.nabl2.terms.stratego.StrategoTerms
import mb.pie.api.Pie
import mb.resource.fs.FSResource
import me.tongfei.progressbar.ProgressBar
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.spoofax.interpreter.terms.ITermFactory
import org.spoofax.terms.io.TAFTermReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * Runs a benchmark.
 */
abstract class BenchmarkRunner(
    private val pie: Pie,
    private val runBenchmarkTask: RunBenchmarkTask,
    private val termFactory: ITermFactory,
) {
    private val log = KotlinLogging.logger {}

    fun run(
        benchmark: Benchmark,
        benchmarkFile: Path,
        projectDir: Path,
        tmpProjectDir: Path,
        sample: Int?,
        seed: Long?
    ): BenchmarkResults {
        val testCaseDir = benchmarkFile.parent.resolve(benchmark.testCaseDirectory)

        // Ensure the tmp project directory is empty, and copy the project
        FileUtils.deleteDirectory(tmpProjectDir.toFile())
        Files.createDirectories(tmpProjectDir.parent)
        Files.copy(projectDir, tmpProjectDir)

        val rnd = Random(seed ?: System.nanoTime())

        // Run the tests
        val results = mutableListOf<BenchmarkResult>()

        // Pick a random sample of test cases, or randomize the order
        val selectedTestCases = benchmark.testCases.sample(sample ?: benchmark.testCases.size, rnd)
        for (testCase in ProgressBar.wrap(selectedTestCases, "Tests")) {
            val result = runTest(benchmark, testCaseDir, projectDir, tmpProjectDir, testCase)
            results.add(result)
        }

        return BenchmarkResults.fromResults(results)
    }

    fun runTest(
        benchmark: Benchmark,
        testCaseDir: Path,
        srcProjectDir: Path,
        dstProjectDir: Path,
        testCase: TestCase
    ): BenchmarkResult {
        log.trace { "Preparing ${testCase.name}..." }
        // Copy the file to the temporary directory
        val srcInputFile = testCaseDir.resolve(testCase.inputFile)
        val dstInputFile = dstProjectDir.resolve(testCase.file)
        Files.createDirectories(dstInputFile.parent)
        Files.copy(srcInputFile, dstInputFile, StandardCopyOption.REPLACE_EXISTING)

        // Read the expected term
        val resExpectedFile = testCaseDir.resolve(testCase.expectedFile)
        val expectedTerm = StrategoTerms(termFactory).fromStratego(TAFTermReader(termFactory).readFromPath(resExpectedFile))

        log.trace { "Running ${testCase.name}..." }
        val result = runBenchmarkTask.run(
            pie,
            benchmark,
            testCaseDir,
            srcProjectDir,
            dstProjectDir,
            testCase,
            expectedTerm,
            FSResource(dstInputFile).key
        )
        log.info { "${testCase.name}: ${result.kind} (${result.totalTime} ms)"}
        // Restore the file
        val origInputFile = srcProjectDir.resolve(testCase.inputFile)
        Files.copy(origInputFile, dstInputFile, StandardCopyOption.REPLACE_EXISTING)
        return result
    }
}
