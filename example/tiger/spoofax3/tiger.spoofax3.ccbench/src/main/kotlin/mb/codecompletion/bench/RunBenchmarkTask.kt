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
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

/**
 * Runs a single benchmark.
 */
class RunBenchmarkTask @Inject constructor(
    private val parseTask: TigerParse,
    private val codeCompletionTask: TigerCodeCompletionTaskDef,
    private val textResourceRegistry: TextResourceRegistry,
    private val termFactory: ITermFactory,
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
//        // Copy the input file into the target project
//        val srcInputFile = input.testCaseDir.resolve(input.testCase.inputFile)
        val dstInputFile = input.targetProjectDir.resolve(input.testCase.file)
//        ctx.require(srcInputFile)
//        ctx.provide(dstInputFile)
//        Files.createDirectories(dstInputFile.parent)
//        Files.copy(srcInputFile, dstInputFile)
//
//        // Read the expected term
//        val resExpectedFile = input.testCaseDir.resolve(input.testCase.expectedFile)
//        ctx.require(resExpectedFile)
//        val expectedTerm = StrategoTerms(termFactory).fromStratego(TAFTermReader(termFactory).readFromPath(resExpectedFile))

//        // We parse the input resource here, such that we don't measure the overhead of parsing the input resource again
        val dstInputResource = ctx.require(dstInputFile)
        parseTask.runParse(ctx, dstInputResource.key)

        // Execute code completion
        val eventHandler = MeasuringCodeCompletionEventHandler()
        val results = ctx.require(
            codeCompletionTask, CodeCompletionTaskDef.Input(
                Region.atOffset(input.testCase.placeholderOffset),
                dstInputResource.key,
                ctx.require(input.targetProjectDir).path,
                eventHandler,
            )
        ).unwrap() as TermCodeCompletionResult

        val success = results.proposals.filterIsInstance<TermCodeCompletionItem>().filter { tryMatchExpectation(results.placeholder, input.expectedTerm, it.term) }.isNotEmpty()

        return BenchmarkResult(
            success,
            results.proposals.toList(),

            eventHandler.parseTime,
            eventHandler.preparationTime,
            eventHandler.analysisTime,
            eventHandler.codeCompletionTime,
            eventHandler.finishingTime,
            eventHandler.totalTime,

            0, // TODO
            0, // TODO
            0, // TODO
            0, // TODO
        )
    }
}
