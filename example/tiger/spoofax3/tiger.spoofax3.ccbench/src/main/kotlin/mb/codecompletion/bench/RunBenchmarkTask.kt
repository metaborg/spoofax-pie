package mb.codecompletion.bench

import mb.codecompletion.bench.utils.runParse
import mb.common.region.Region
import mb.jsglr.pie.JsglrParseTaskDef
import mb.nabl2.terms.ITerm
import mb.pie.api.ExecContext
import mb.pie.api.Pie
import mb.pie.api.TaskDef
import mb.resource.ResourceKey
import mb.statix.TermCodeCompletionItem
import mb.statix.TermCodeCompletionResult
import mb.statix.codecompletion.pie.CodeCompletionTaskDef
import mb.statix.codecompletion.pie.MeasuringCodeCompletionEventHandler
import mu.KotlinLogging
import java.io.Serializable
import java.nio.file.Path

/**
 * Runs a single benchmark.
 */
abstract class RunBenchmarkTask(
    private val parseTask: JsglrParseTaskDef,
    private val codeCompletionTask: CodeCompletionTaskDef,
) : TaskDef<RunBenchmarkTask.Input, BenchmarkResult> {

    data class Input(
        val benchmark: Benchmark,
        val testCaseDir: Path,
        val sourceProjectDir: Path,
        val targetProjectDir: Path,
        val testCase: TestCase,
        val expectedTerm: ITerm,
    ): Serializable

    /**
     * Runs this task.
     *
     * @param benchmarkCase the benchmark to run
     * @return the benchmark result
     */
    fun run(
        pie: Pie,
        benchmark: Benchmark,
        testCaseDir: Path,
        sourceProjectDir: Path,
        targetProjectDir: Path,
        testCase: TestCase,
        expectedTerm: ITerm,
        dstInputResourceKey: ResourceKey,
    ): BenchmarkResult {
        pie.newSession().use { session ->
            val topDownSession = session.updateAffectedBy(setOf(
                dstInputResourceKey
            ))
            return topDownSession.requireWithoutObserving(this.createTask(Input(
                benchmark,
                testCaseDir,
                sourceProjectDir,
                targetProjectDir,
                testCase,
                expectedTerm
            )))
        }
    }

    companion object {
        /** Number of ns per ms. */
        private val NS_PER_MS: Double = 1000_000.0
    }

    private val log = KotlinLogging.logger {}

    override fun getId(): String = RunBenchmarkTask::class.java.name

    override fun exec(ctx: ExecContext, input: Input): BenchmarkResult {
        val dstInputFile = input.targetProjectDir.resolve(input.testCase.file)

        // We parse the input resource here, such that we don't measure the overhead of parsing the input resource again
        val dstInputResource = ctx.require(dstInputFile)
        parseTask.runParse(ctx, dstInputResource.key)

        // Execute code completion
        var kind: BenchmarkResultKind
        val eventHandler = MeasuringCodeCompletionEventHandler()
        var results: TermCodeCompletionResult? = null
        try {
            results = ctx.require(
                codeCompletionTask, CodeCompletionTaskDef.Input(
                    Region.atOffset(input.testCase.placeholderOffset),
                    dstInputResource.key,
                    ctx.require(input.targetProjectDir).path,
                    eventHandler,
                )
            ).unwrap() as TermCodeCompletionResult

            kind = if (!results.proposals.isEmpty) {
                val extProposals = results.proposals.filterIsInstance<TermCodeCompletionItem>()
                val success = extProposals.filter { tryMatchExpectation(results.placeholder, input.expectedTerm, it.term) }.isNotEmpty()
                if (success) {
                    BenchmarkResultKind.Success
                } else {
                    log.warn { "Expected: ${input.expectedTerm}, got: ${extProposals.joinToString { "${it.label} (${it.term})" }}" }
                    BenchmarkResultKind.Failed
                }
            } else {
                log.warn { "Expected: ${input.expectedTerm}, got: <nothing>" }
                BenchmarkResultKind.NoResults
            }
        } catch (ex: IllegalStateException) {
            kind = if (ex.message?.contains("input program validation failed") == true) {
                log.warn { "Analysis failed: ${ex.message}" }
                BenchmarkResultKind.AnalysisFailed
            } else {
                log.warn(ex) { "Error running test." }
                BenchmarkResultKind.Error
            }
        }

        return BenchmarkResult(
            input.testCase.name,
            kind,
            results?.proposals?.toList() ?: emptyList(),

            (eventHandler.parseTime ?: -1).toDouble() / NS_PER_MS,
            (eventHandler.preparationTime ?: -1).toDouble() / NS_PER_MS,
            (eventHandler.analysisTime ?: -1).toDouble() / NS_PER_MS,
            (eventHandler.codeCompletionTime ?: -1).toDouble() / NS_PER_MS,
            (eventHandler.finishingTime ?: -1).toDouble() / NS_PER_MS,
            (eventHandler.totalTime ?: -1).toDouble() / NS_PER_MS,

            (0).toDouble() / NS_PER_MS, // TODO
            (0).toDouble() / NS_PER_MS, // TODO
            (0).toDouble() / NS_PER_MS, // TODO
            (0).toDouble() / NS_PER_MS, // TODO
        )
    }
}
