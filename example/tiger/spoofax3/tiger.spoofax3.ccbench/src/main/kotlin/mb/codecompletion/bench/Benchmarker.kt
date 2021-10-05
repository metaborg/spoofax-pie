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
class Benchmarker constructor(
    private val runBenchmarkTask: RunBenchmarkTask,
    private val prepareBenchmarkTask: PrepareBenchmarkTask,
    private val benchmarkReaderWriter: BenchmarkReaderWriter,
) {

//    /**
//     * Prepares all benchmark cases.
//     *
//     * @param pie PIE
//     * @param inputFile the input file path
//     */
//    fun prepareAll(pie: Pie, inputFile: Path): List<BenchmarkCase> {
//        return prepareBenchmarkTask.run(pie, inputFile)
//    }

//    /**
//     * Runs all given benchmark cases.
//     *
//     * @param pie PIE
//     * @param benchmarks the benchmark cases to run
//     * @return the results of the benchmark cases
//     */
//    fun runAll(pie: Pie, benchmarks: Iterable<BenchmarkCase>): BenchmarkResults {
//        val results = mutableListOf<BenchmarkResult>()
//        for (benchmark in benchmarks) {
//            // NOTE: This creates a new PIE session every time.
//            //  Is this the correct way to do it?
//            val result = runBenchmarkTask.run(pie, benchmark)
//            results.add(result)
//        }
//        return BenchmarkResults(
//            results
//        )
//    }

    /**
     * Loads all benchmarks from the given directory.
     *
     * @param directory the directory to load from
     * @return the list of loaded benchmark cases
     */
    fun loadAll(directory: Path): List<BenchmarkCase> {
        val benchmarkFiles = Files.list(directory).filter { it.extension == ".yml" }
        return benchmarkFiles.map {
            benchmarkReaderWriter.read(
                it,
                it.fileName.withExtension { ext -> ext.substringBeforeLast(".yml") }.toString(),
                directory
            )
        }.toList()
    }

    /**
     * Saves all benchmarks to the given directory.
     *
     * @param benchmarkCases the benchmark cases to save
     * @param directory the directory to save to
     */
    fun saveAll(benchmarkCases: Iterable<BenchmarkCase>, directory: Path) {
        for (benchmark in benchmarkCases) {
            val benchmarkFile = directory.resolve(benchmark.filename).withExtension { "$it.yml" }
            benchmarkReaderWriter.write(benchmark, benchmarkFile, benchmark.filename, directory)
        }
    }

}
