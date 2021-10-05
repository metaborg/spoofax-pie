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

            val success = results.proposals.filterIsInstance<TermCodeCompletionItem>().filter { tryMatchExpectation(results.placeholder, input.expectedTerm, it.term) }.isNotEmpty()
            kind = if (success) BenchmarkResultKind.Success else BenchmarkResultKind.Failed
        } catch (ex: IllegalStateException) {
            kind = if (ex.message?.contains("input program validation failed") == true) {
                BenchmarkResultKind.AnalysisFailed
            } else {
                BenchmarkResultKind.Error
            }
        }

        return BenchmarkResult(
            kind,
            results?.proposals?.toList() ?: emptyList(),

            eventHandler.parseTime ?: -1,
            eventHandler.preparationTime ?: -1,
            eventHandler.analysisTime ?: -1,
            eventHandler.codeCompletionTime ?: -1,
            eventHandler.finishingTime ?: -1,
            eventHandler.totalTime ?: -1,

            0, // TODO
            0, // TODO
            0, // TODO
            0, // TODO
        )
    }
}
