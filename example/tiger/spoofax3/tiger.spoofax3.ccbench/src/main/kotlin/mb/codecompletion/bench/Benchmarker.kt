package mb.codecompletion.bench

import mb.codecompletion.bench.utils.withExtension
import mb.pie.api.Pie
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.extension
import kotlin.streams.toList

/**
 * Prepares and/or runs one or more benchmarks and gathers the results.
 */
class Benchmarker @Inject constructor(
    private val runBenchmarkTask: RunBenchmarkTask,
    private val prepareBenchmarkTask: PrepareBenchmarkTask,
    private val benchmarkReaderWriter: BenchmarkReaderWriter,
) {

    /**
     * Prepares all benchmarks.
     *
     * @param pie PIE
     * @param inputFile the input file path
     */
    fun prepareAll(pie: Pie, inputFile: Path): List<Benchmark> {
        return prepareBenchmarkTask.run(pie, inputFile)
    }

    /**
     * Runs all given benchmarks.
     *
     * @param pie PIE
     * @param benchmarks the benchmarks to run
     * @return the results of the benchmarks
     */
    fun runAll(pie: Pie, benchmarks: Iterable<Benchmark>): BenchmarkResults {
        val results = mutableListOf<BenchmarkResult>()
        for (benchmark in benchmarks) {
            // NOTE: This creates a new PIE session every time.
            //  Is this the correct way to do it?
            val result = runBenchmarkTask.run(pie, benchmark)
            results.add(result)
        }
        return BenchmarkResults(
            results
        )
    }

    // TODO: loading and saving should not be here
    /**
     * Loads all benchmarks from the given directory.
     *
     * @param directory the directory to load from
     * @return the list of loaded benchmarks
     */
    fun loadAll(directory: Path): List<Benchmark> {
        val benchmarkFiles = Files.list(directory).filter { it.extension == ".yml" }
        return benchmarkFiles.map {
            benchmarkReaderWriter.read(
                it,
                it.fileName.withExtension { ext -> ext.substringBeforeLast(".yml") }.toString(),
                directory
            )
        }.toList()
    }

    // TODO: loading and saving should not be here
    /**
     * Saves all benchmarks to the given directory.
     *
     * @param benchmarks the benchmarks to save
     * @param directory the directory to save to
     */
    fun saveAll(benchmarks: Iterable<Benchmark>, directory: Path) {
        for (benchmark in benchmarks) {
            val benchmarkFile = directory.resolve(benchmark.filename).withExtension { "$it.yml" }
            benchmarkReaderWriter.write(benchmark, benchmarkFile, benchmark.filename, directory)
        }
    }

}
