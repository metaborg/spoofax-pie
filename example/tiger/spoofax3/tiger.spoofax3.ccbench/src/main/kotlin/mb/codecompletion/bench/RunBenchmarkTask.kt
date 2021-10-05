package mb.codecompletion.bench

import mb.codecompletion.bench.utils.runParse
import mb.common.region.Region
import mb.jsglr.pie.JsglrParseTaskInput
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
import org.spoofax.interpreter.terms.IStrategoTerm
import org.spoofax.interpreter.terms.ITermFactory
import javax.inject.Inject
import kotlin.streams.asSequence

/**
 * Runs a single benchmark.
 */
class RunBenchmarkTask @Inject constructor(
    private val parseTask: TigerParse,
    private val codeCompletionTask: TigerCodeCompletionTaskDef,
    private val textResourceRegistry: TextResourceRegistry,
    private val termFactory: ITermFactory,
) : TaskDef<Benchmark, BenchmarkResult> {

    /**
     * Runs this task.
     *
     * @param benchmark the benchmark to run
     * @return the benchmark result
     */
    fun run(pie: Pie, benchmark: Benchmark): BenchmarkResult {
        pie.newSession().use { session ->
            val topDownSession = session.updateAffectedBy(emptySet())
            return topDownSession.require(this.createTask(benchmark))
        }
    }

    override fun getId(): String = RunBenchmarkTask::class.java.name

    override fun exec(ctx: ExecContext, benchmark: Benchmark): BenchmarkResult {
        val inputResource = textResourceRegistry.createResource(benchmark.inputText, benchmark.filename)
        val expectedTerm = StrategoTerms(termFactory).fromStratego(benchmark.expectedTerm)
        val eventHandler = MeasuringCodeCompletionEventHandler()

        // We parse the input resource here, such that we don't measure the overhead of parsing the input resource again
        parseTask.runParse(ctx, inputResource.key)

        // Execute code completion
        val results = ctx.require(
            codeCompletionTask, CodeCompletionTaskDef.Input(
                Region.atOffset(benchmark.placeholderOffset),
                inputResource.key,
                null, // TODO
                eventHandler,
            )
        ).unwrap() as TermCodeCompletionResult

        val success = results.proposals.filterIsInstance<TermCodeCompletionItem>().filter { tryMatchExpectation(results.placeholder, expectedTerm, it.term) }.isNotEmpty()

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
