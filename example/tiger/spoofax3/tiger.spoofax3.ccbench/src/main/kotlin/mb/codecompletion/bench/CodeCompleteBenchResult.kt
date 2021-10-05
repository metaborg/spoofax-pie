package mb.codecompletion.bench

import mb.common.codecompletion.CodeCompletionItem
import java.io.Serializable

/**
 * Completeness test results.
 */
@Deprecated("Use BenchmarkResult")
data class CodeCompleteBenchResult(
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

  val success: Boolean,
  val results: List<CodeCompletionItem>,
) : Serializable
