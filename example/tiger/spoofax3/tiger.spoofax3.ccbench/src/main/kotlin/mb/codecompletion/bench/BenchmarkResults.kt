package mb.codecompletion.bench

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
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
    val results: List<BenchmarkResult>
) {

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
        CSVPrinter(writer, format).use { printer ->
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
