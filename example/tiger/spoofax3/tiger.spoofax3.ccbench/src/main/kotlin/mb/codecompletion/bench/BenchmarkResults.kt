package mb.codecompletion.bench

import mb.codecompletion.bench.utils.NonClosingWriter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.commons.math3.stat.descriptive.rank.Median
import java.io.OutputStream
import java.io.Writer
import java.nio.file.Path
import kotlin.io.path.bufferedWriter

/**
 * A set of benchmark results.
 *
 * @param results the individual benchmark results
 */
data class BenchmarkResults(
    val mean: BenchmarkResult,
    val p10: BenchmarkResult,
    val median: BenchmarkResult,
    val p90: BenchmarkResult,
    val results: List<BenchmarkResult>
) {

    companion object {
        /**
         * Creates a [BenchmarkResults] objects from the given results.
         *
         * @param results the results
         * @return the [BenchmarkResults] object
         */
        fun fromResults(results: List<BenchmarkResult>): BenchmarkResults {
            val successResults = results.filter { it.kind == BenchmarkResultKind.Success }

            val parseTimeStats = DescriptiveStatistics()
            val preparationTimeStats = DescriptiveStatistics()
            val analyzeTimeStats = DescriptiveStatistics()
            val codeCompletionTimeStats = DescriptiveStatistics()
            val finishingTimeStats = DescriptiveStatistics()
            val totalTimeStats = DescriptiveStatistics()

            val expandRulesTimeStats = DescriptiveStatistics()
            val expandInjectionsTimeStats = DescriptiveStatistics()
            val expandQueriesTimeStats = DescriptiveStatistics()
            val expandDeterministicTimeStats = DescriptiveStatistics()

            for (result in successResults) {
                parseTimeStats.addValue(result.parseTime)
                preparationTimeStats.addValue(result.preparationTime)
                analyzeTimeStats.addValue(result.analyzeTime)
                codeCompletionTimeStats.addValue(result.codeCompletionTime)
                finishingTimeStats.addValue(result.finishingTime)
                totalTimeStats.addValue(result.totalTime)

                expandRulesTimeStats.addValue(result.expandRulesTime)
                expandInjectionsTimeStats.addValue(result.expandInjectionsTime)
                expandQueriesTimeStats.addValue(result.expandQueriesTime)
                expandDeterministicTimeStats.addValue(result.expandDeterministicTime)
            }

            fun getBenchmarkResult(name: String, f: (DescriptiveStatistics) -> Double): BenchmarkResult
              = BenchmarkResult(
                name, BenchmarkResultKind.Success, emptyList(),
                f(parseTimeStats),
                f(preparationTimeStats),
                f(analyzeTimeStats),
                f(codeCompletionTimeStats),
                f(finishingTimeStats),
                f(totalTimeStats),

                f(expandRulesTimeStats),
                f(expandInjectionsTimeStats),
                f(expandQueriesTimeStats),
                f(expandDeterministicTimeStats),
            )

            val mean = getBenchmarkResult("Mean") { s -> s.mean }                       // MEAN(data)
            val p10 = getBenchmarkResult("Percentile10") { s -> s.getPercentile(10.0) } // PERCENTILE.EXC(data, 0.1)
            val median = getBenchmarkResult("Median") { s -> s.getPercentile(50.0) }    // MEDIAN(data)
            val p90 = getBenchmarkResult("Percentile90") { s -> s.getPercentile(90.0) } // PERCENTILE.EXC(data, 0.9)

            return BenchmarkResults(
                mean, p10, median, p90, results
            )
        }
    }

    /**
     * Writes the benchmark results as CSV to the specified writer.
     *
     * @param writer the writer
     */
    fun writeAsCsv(writer: Writer) {
        val format = CSVFormat.Builder.create(CSVFormat.EXCEL)
            .setDelimiter(';')
            .setHeader(*BenchmarkResult.csvHeaders)
            .setAutoFlush(true)
            .build()
        writer.write("sep=;\n")
        CSVPrinter(NonClosingWriter(writer), format).use { printer ->
            printer.printRecord(*this.mean.toCsvArray())
            printer.printRecord(*this.p10.toCsvArray())
            printer.printRecord(*this.median.toCsvArray())
            printer.printRecord(*this.p90.toCsvArray())
        }
        writer.write("\n")
        CSVPrinter(NonClosingWriter(writer), format).use { printer ->
            for (result in results) {
                printer.printRecord(*result.toCsvArray())
            }
        }
    }

    /**
    * Writes the benchmark results as CSV to the specified output stream.
     *
     * @param stream the output stream
     */
    fun writeAsCsv(stream: OutputStream) {
        stream.bufferedWriter().use { writer -> writeAsCsv(writer) }
    }

    /**
     * Writes the benchmark results as CSV to the specified path.
     *
     * @param file the output file path
     */
    fun writeAsCsv(file: Path) {
        file.bufferedWriter().use { writer -> writeAsCsv(writer) }
    }

}
