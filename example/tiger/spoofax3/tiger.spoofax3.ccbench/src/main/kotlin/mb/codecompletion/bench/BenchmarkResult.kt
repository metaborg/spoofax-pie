package mb.codecompletion.bench

import mb.common.codecompletion.CodeCompletionItem
import java.io.Serializable

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

    val parseTime: Long,
    val preparationTime: Long,
    val analyzeTime: Long,
    val codeCompletionTime: Long,
    val finishingTime: Long,
    val totalTime: Long,

    val expandRulesTime: Long,
    val expandInjectionsTime: Long,
    val expandQueriesTime: Long,
    val expandDeterministicTime: Long,
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
    }

    /**
     * Returns this benchmark result as an array of values for the CSV.
     */
    fun toCsvArray(): Array<Any?> = arrayOf(
        this.name,
        this.kind,
        this.results.size,

        this.parseTime,               // ms
        this.preparationTime,         // ms
        this.analyzeTime,             // ms
        this.codeCompletionTime,      // ms
        this.finishingTime,           // ms
        this.totalTime,               // ms

        this.expandRulesTime,         // ms
        this.expandInjectionsTime,    // ms
        this.expandQueriesTime,       // ms
        this.expandDeterministicTime, // ms
    )
}
