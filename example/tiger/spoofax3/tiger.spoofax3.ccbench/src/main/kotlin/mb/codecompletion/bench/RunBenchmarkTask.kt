package mb.codecompletion.bench

import mb.codecompletion.bench.utils.runParse
import mb.common.region.Region
import mb.nabl2.terms.ITerm
import mb.nabl2.terms.stratego.StrategoTerms
import mb.pie.api.ExecContext
import mb.pie.api.Pie
import mb.pie.api.TaskDef
import mb.resource.ResourceKey
import mb.resource.text.TextResourceRegistry
import mb.statix.TermCodeCompletionItem
import mb.statix.TermCodeCompletionResult
import mb.statix.codecompletion.pie.CodeCompletionTaskDef
import mb.statix.codecompletion.pie.MeasuringCodeCompletionEventHandler
import mb.tiger.task.TigerCodeCompletionTaskDef
import mb.tiger.task.TigerParse
import org.spoofax.interpreter.terms.ITermFactory
import org.spoofax.terms.io.TAFTermReader
import java.io.Serializable
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

/**
 * Runs a single benchmark.
 */
class RunBenchmarkTask @Inject constructor(
    private val parseTask: TigerParse,
    private val codeCompletionTask: TigerCodeCompletionTaskDef,
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
                    println("Expected: ${input.expectedTerm}")
                    println("Got: ${extProposals.joinToString { "${it.label} (${it.term})" }}")
                    BenchmarkResultKind.Failed
                }
            } else {
                println("Expected: ${input.expectedTerm}")
                println("Got: <nothing>")
                BenchmarkResultKind.NoResults
            }
        } catch (ex: IllegalStateException) {
            kind = if (ex.message?.contains("input program validation failed") == true) {
                BenchmarkResultKind.AnalysisFailed
            } else {
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
