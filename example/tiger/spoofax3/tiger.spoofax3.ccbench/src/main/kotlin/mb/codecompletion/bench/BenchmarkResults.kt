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
    val mean: BenchmarkSummary,
    val p01: BenchmarkSummary,
    val p05: BenchmarkSummary,
    val p10: BenchmarkSummary,
    val median: BenchmarkSummary,
    val p90: BenchmarkSummary,
    val p95: BenchmarkSummary,
    val p99: BenchmarkSummary,
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

            fun getBenchmarkSummary(name: String, f: (DescriptiveStatistics) -> Double): BenchmarkSummary
              = BenchmarkSummary(name, successResults.size, BenchmarkResult(
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
            ))

            val mean = getBenchmarkSummary("Mean") { s -> s.mean }                       // MEAN(data)
            val p01 = getBenchmarkSummary("Percentile01") { s -> s.getPercentile(01.0) } // PERCENTILE.EXC(data, 0.01)
            val p05 = getBenchmarkSummary("Percentile05") { s -> s.getPercentile(05.0) } // PERCENTILE.EXC(data, 0.05)
            val p10 = getBenchmarkSummary("Percentile10") { s -> s.getPercentile(10.0) } // PERCENTILE.EXC(data, 0.10)
            val median = getBenchmarkSummary("Median") { s -> s.getPercentile(50.0) }    // MEDIAN(data)
            val p90 = getBenchmarkSummary("Percentile90") { s -> s.getPercentile(90.0) } // PERCENTILE.EXC(data, 0.90)
            val p95 = getBenchmarkSummary("Percentile95") { s -> s.getPercentile(95.0) } // PERCENTILE.EXC(data, 0.95)
            val p99 = getBenchmarkSummary("Percentile99") { s -> s.getPercentile(99.0) } // PERCENTILE.EXC(data, 0.99)

            return BenchmarkResults(
                mean, p01, p05, p10, median, p90, p95, p99, results
            )
        }
    }

    /**
     * Writes the benchmark results as CSV to the specified writer.
     *
     * @param writer the writer
     */
    fun writeAsCsv(writer: Writer) {
        writer.write("sep=;\n")
        val format = CSVFormat.Builder.create(CSVFormat.EXCEL)
            .setDelimiter(';')
            .setAutoFlush(true)
            .build()
        val format1 = CSVFormat.Builder.create(format)
            .setHeader(*BenchmarkSummary.csvHeaders)
            .build()
        CSVPrinter(NonClosingWriter(writer), format1).use { printer ->
            printer.printRecord(*this.mean.toCsvArray())
            printer.printRecord(*this.p01.toCsvArray())
            printer.printRecord(*this.p05.toCsvArray())
            printer.printRecord(*this.p10.toCsvArray())
            printer.printRecord(*this.median.toCsvArray())
            printer.printRecord(*this.p90.toCsvArray())
            printer.printRecord(*this.p95.toCsvArray())
            printer.printRecord(*this.p99.toCsvArray())
        }

        writer.write("\n")
        val format2 = CSVFormat.Builder.create(format)
            .setHeader(*BenchmarkSummary.csvHeaders)
            .build()
        CSVPrinter(NonClosingWriter(writer), format2).use { printer ->
            // Start with the successful results
            for (result in results.filter { it.kind == BenchmarkResultKind.Success }) {
                printer.printRecord(*result.toCsvArray())
            }
            // Other kinds of results
            for (kind in BenchmarkResultKind.values().filter { it != BenchmarkResultKind.Success }) {
                val resultsOfKind = results.filter { it.kind == kind }
                if (resultsOfKind.isNotEmpty()) {
                    writer.write("\n")
                    for (result in resultsOfKind) {
                        printer.printRecord(*result.toCsvArray())
                    }
                }
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
