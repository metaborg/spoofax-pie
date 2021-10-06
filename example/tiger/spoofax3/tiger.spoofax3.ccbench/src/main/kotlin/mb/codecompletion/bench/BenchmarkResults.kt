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
                parseTimeStats.addValue(result.parseTime.toDouble())
                preparationTimeStats.addValue(result.preparationTime.toDouble())
                analyzeTimeStats.addValue(result.analyzeTime.toDouble())
                codeCompletionTimeStats.addValue(result.codeCompletionTime.toDouble())
                finishingTimeStats.addValue(result.finishingTime.toDouble())
                totalTimeStats.addValue(result.totalTime.toDouble())

                expandRulesTimeStats.addValue(result.expandRulesTime.toDouble())
                expandInjectionsTimeStats.addValue(result.expandInjectionsTime.toDouble())
                expandQueriesTimeStats.addValue(result.expandQueriesTime.toDouble())
                expandDeterministicTimeStats.addValue(result.expandDeterministicTime.toDouble())
            }

            fun getBenchmarkResult(name: String, f: (DescriptiveStatistics) -> Double): BenchmarkResult
              = BenchmarkResult(
                name, BenchmarkResultKind.Success, emptyList(),
                f(parseTimeStats).toLong(),
                f(preparationTimeStats).toLong(),
                f(analyzeTimeStats).toLong(),
                f(codeCompletionTimeStats).toLong(),
                f(finishingTimeStats).toLong(),
                f(totalTimeStats).toLong(),

                f(expandRulesTimeStats).toLong(),
                f(expandInjectionsTimeStats).toLong(),
                f(expandQueriesTimeStats).toLong(),
                f(expandDeterministicTimeStats).toLong(),
            )

            val mean = getBenchmarkResult("Mean") { s -> s.mean }
            val p10 = getBenchmarkResult("Percentile10") { s -> s.getPercentile(10.0) }
            val median = getBenchmarkResult("Median") { s -> s.getPercentile(50.0) }
            val p90 = getBenchmarkResult("Percentile90") { s -> s.getPercentile(90.0) }

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
            .setHeader(*BenchmarkResult.csvHeaders)
            .setAutoFlush(true)
            .build()
        writer.write("sep=,\n")
        CSVPrinter(NonClosingWriter(writer), format).use { printer ->
            printer.printRecord(*this.mean.toCsvArray())
            printer.printRecord(*this.p10.toCsvArray())
            printer.printRecord(*this.median.toCsvArray())
            printer.printRecord(*this.p90.toCsvArray())
        }
        CSVPrinter(NonClosingWriter(writer), format).use { printer ->
            writer.write("\n")
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
