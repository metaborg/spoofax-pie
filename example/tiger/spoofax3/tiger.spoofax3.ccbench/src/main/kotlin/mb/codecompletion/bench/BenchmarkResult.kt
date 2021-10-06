package mb.codecompletion.bench

import mb.common.codecompletion.CodeCompletionItem
import java.io.Serializable
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * A single benchmark result.
 *
 * @property name The name of the benchmark test.
 * @property kind The result of the benchmark.
 * @property results The code completion proposals resulting from the benchmark.
 *
 * @property parseTime Time spent on parsing the input file with the placeholder; in ms.
 * @property preparationTime Time spent on preparing the input AST by explicating and upgrading it; in ms.
 * @property analyzeTime Time spent on analyzing the input AST with the placeholder; in ms.
 * @property codeCompletionTime Time spent on the code completion task; in ms.
 * @property finishingTime Time spent on finishing the code completion proposals, by downgrading, implicating, and pretty-printing them; in ms.
 * @property totalTime Time spent on the whole code completion, from parsing to finishing; in ms.
 *
 * @property expandRulesTime Time spent on expanding rules; in ms.
 * @property expandInjectionsTime Time spent on expanding injections; in ms.
 * @property expandQueriesTime Time spent on expanding queries; in ms.
 * @property expandDeterministicTime Time spent on expanding deterministically; in ms.
 */
data class BenchmarkResult(
    val name: String,
    val kind: BenchmarkResultKind,
    val results: List<CodeCompletionItem>,

    val parseTime: Double,
    val preparationTime: Double,
    val analyzeTime: Double,
    val codeCompletionTime: Double,
    val finishingTime: Double,
    val totalTime: Double,

    val expandRulesTime: Double,
    val expandInjectionsTime: Double,
    val expandQueriesTime: Double,
    val expandDeterministicTime: Double,
): Serializable {

    val success: Boolean get() = kind == BenchmarkResultKind.Success

    companion object {
        /**
         * The headers for the CSV.
         */
        val csvHeaders = arrayOf(
            "Name",
            "Kind",
            "NumberOfResults",

            "ParseTime",
            "PreparationTime",
            "AnalyzeTime",
            "CodeCompletionTime",
            "FinishingTime",
            "TotalTime",

            "ExpandRulesTime",
            "ExpandInjectionsTime",
            "ExpandQueriesTime",
            "ExpandDeterministicTime",
        )

        val decimalFormatter: DecimalFormat = createDecimalFormatter()

        private fun createDecimalFormatter(): DecimalFormat {
            val otherSymbols = DecimalFormatSymbols(Locale.ROOT)
            otherSymbols.decimalSeparator = ','
            otherSymbols.groupingSeparator = '.'
            return DecimalFormat("#0.00", otherSymbols)
        }
    }

    /**
     * Returns this benchmark result as an array of values for the CSV.
     */
    fun toCsvArray(): Array<Any?> {
        return arrayOf(
            this.name,
            this.kind,
            this.results.size,

            decimalFormatter.format(this.parseTime),               // ms
            decimalFormatter.format(this.preparationTime),         // ms
            decimalFormatter.format(this.analyzeTime),             // ms
            decimalFormatter.format(this.codeCompletionTime),      // ms
            decimalFormatter.format(this.finishingTime),           // ms
            decimalFormatter.format(this.totalTime),               // ms

            decimalFormatter.format(this.expandRulesTime),         // ms
            decimalFormatter.format(this.expandInjectionsTime),    // ms
            decimalFormatter.format(this.expandQueriesTime),       // ms
            decimalFormatter.format(this.expandDeterministicTime), // ms
        )
    }
}

data class BenchmarkSummary(
    val name: String,
    val count: Int,
    val result: BenchmarkResult
) {
    companion object {
        /**
         * The headers for the CSV.
         */
        val csvHeaders = getCsvHeadersInternal()

        private fun getCsvHeadersInternal(): Array<String> {
            val addHeaderCount = 3
            val remHeaderCount = 3
            val resultArr = BenchmarkResult.csvHeaders
            val arr = Array(addHeaderCount + (resultArr.size - remHeaderCount)) { "" }
            arr[0] = "Name"
            arr[1] = ""
            arr[2] = "TestCount"
            resultArr.copyInto(arr, addHeaderCount, remHeaderCount, arr.size)
            return arr
        }
    }

    /**
     * Returns this benchmark result as an array of values for the CSV.
     */
    fun toCsvArray(): Array<Any?> {
        val addHeaderCount = 3
        val remHeaderCount = 3
        val resultArr = result.toCsvArray()
        val arr = arrayOfNulls<Any>(addHeaderCount + (resultArr.size - remHeaderCount))
        arr[0] = this.name
        arr[1] = ""
        arr[2] = this.count
        resultArr.copyInto(arr, addHeaderCount, remHeaderCount, arr.size)
        return arr
    }
}
