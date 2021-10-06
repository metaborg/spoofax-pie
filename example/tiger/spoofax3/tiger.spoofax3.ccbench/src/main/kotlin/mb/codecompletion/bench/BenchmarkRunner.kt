package mb.codecompletion.bench

import mb.nabl2.terms.stratego.StrategoTerms
import mb.pie.api.Pie
import mb.resource.fs.FSResource
import org.apache.commons.io.FileUtils
import org.spoofax.interpreter.terms.ITermFactory
import org.spoofax.terms.io.TAFTermReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.inject.Inject

/**
 * Runs a benchmark.
 */
class BenchmarkRunner @Inject constructor(
    private val pie: Pie,
    private val runBenchmarkTask: RunBenchmarkTask,
    private val termFactory: ITermFactory,
) {

    fun run(
        benchmark: Benchmark,
        benchmarkFile: Path,
        projectDir: Path,
        tmpProjectDir: Path,
    ): BenchmarkResults {
        val testCaseDir = benchmarkFile.parent.resolve(benchmark.testCaseDirectory)

        // Copy the project
        FileUtils.deleteDirectory(tmpProjectDir.toFile())
        Files.createDirectories(tmpProjectDir.parent)
        Files.copy(projectDir, tmpProjectDir)

        // Run the tests
        val results = mutableListOf<BenchmarkResult>()
        for (testCase in benchmark.testCases) {
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
        println("Preparing ${testCase.name}...")
        // Copy the file to the temporary directory
        val srcInputFile = testCaseDir.resolve(testCase.inputFile)
        val dstInputFile = dstProjectDir.resolve(testCase.file)
        Files.createDirectories(dstInputFile.parent)
        Files.copy(srcInputFile, dstInputFile, StandardCopyOption.REPLACE_EXISTING)

        // Read the expected term
        val resExpectedFile = testCaseDir.resolve(testCase.expectedFile)
        val expectedTerm = StrategoTerms(termFactory).fromStratego(TAFTermReader(termFactory).readFromPath(resExpectedFile))

        println("Running ${testCase.name}...")
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
        println("Finished ${testCase.name}: $result")
        return result
    }
}
