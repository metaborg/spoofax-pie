package mb.codecompletion.bench

import java.io.Serializable

/**
 * Completeness test results.
 */
data class CompletenessTestResult(
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

  val numberOfResults: Int,
) : Serializable
