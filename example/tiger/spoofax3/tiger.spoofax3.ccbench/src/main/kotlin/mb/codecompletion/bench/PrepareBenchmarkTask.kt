package mb.codecompletion.bench

import mb.codecompletion.bench.utils.runParse
import mb.codecompletion.bench.utils.sample
import mb.codecompletion.bench.utils.withExtension
import mb.codecompletion.bench.utils.withName
import mb.common.region.Region
import mb.common.result.Result
import mb.common.util.ListView
import mb.constraint.pie.ConstraintAnalyzeTaskDef
import mb.jsglr.pie.JsglrParseTaskDef
import mb.nabl2.terms.stratego.TermOrigin
import mb.pie.api.ExecContext
import mb.pie.api.Pie
import mb.pie.api.Supplier
import mb.pie.api.TaskDef
import mb.resource.text.TextResourceRegistry
import mb.stratego.pie.AstStrategoTransformTaskDef
import me.tongfei.progressbar.ProgressBar
import mu.KotlinLogging
import org.spoofax.interpreter.terms.IStrategoAppl
import org.spoofax.interpreter.terms.IStrategoList
import org.spoofax.interpreter.terms.IStrategoPlaceholder
import org.spoofax.interpreter.terms.IStrategoTerm
import org.spoofax.interpreter.terms.IStrategoTuple
import org.spoofax.interpreter.terms.ITermFactory
import org.spoofax.terms.io.SimpleTextTermWriter
import org.spoofax.terms.util.TermUtils
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * Runs a single benchmark.
 */
abstract class PrepareBenchmarkTask(
    private val parseTask: JsglrParseTaskDef,
    private val analyzeTask: ConstraintAnalyzeTaskDef,
    private val explicateTask: AstStrategoTransformTaskDef,
    private val implicateTask: AstStrategoTransformTaskDef,
    private val upgradePlaceholdersTask: AstStrategoTransformTaskDef,
    private val downgradePlaceholdersTask: AstStrategoTransformTaskDef,
    private val prettyPrintTask: AstStrategoTransformTaskDef,
    private val textResourceRegistry: TextResourceRegistry,
    private val termFactory: ITermFactory,
    private val termWriter: SimpleTextTermWriter,
) : TaskDef<PrepareBenchmarkTask.Input, ListView<TestCase>> {

    /**
     * The input arguments for the task.
     *
     * @property projectDir the project directory
     * @property inputFile the input language file, relative to the project directory
     * @property testCaseDir the test case directory
     */
    data class Input(
        val projectDir: Path,
        val inputFile: Path,
        val testCaseDir: Path,
        val sample: Int?,
        val rnd: Random,
    ): Serializable

    private val log = KotlinLogging.logger {}

    /**
     * Runs this task.
     *
     * @param inputFile the task input file
     * @return the benchmark
     */
    fun run(pie: Pie, projectDir: Path, inputFile: Path, testCaseDir: Path, sample: Int?, rnd: Random): List<TestCase> {
        pie.newSession().use { session ->
            val topDownSession = session.updateAffectedBy(emptySet())
            return try {
                topDownSession.require(this.createTask(Input(projectDir, inputFile, testCaseDir, sample, rnd))).toList()
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyList()
            }
        }
    }

    override fun getId(): String = PrepareBenchmarkTask::class.java.name

    override fun exec(ctx: ExecContext, input: Input): ListView<TestCase> {
        val originalName = input.inputFile.withExtension("").toString()
        val resInputFile = input.projectDir.resolve(input.inputFile)
        val inputResource = ctx.require(resInputFile)

        val ast: IStrategoTerm
        try {
            // Parse the input
            ast = parseTask.runParse(ctx, inputResource.key)
        } catch (ex: Exception) {
            log.warn { "Failed to parse $originalName" }
            ex.printStackTrace()
            return ListView.of()
        }

        try {
            // Analyze the file
            val result = ctx.require(analyzeTask, ConstraintAnalyzeTaskDef.Input(
                inputResource.key
            ) { Result.ofOk<IStrategoTerm, Exception>(ast) }).unwrap()
            if (result.result.messages.containsError()) {
                log.warn { "Failed to analyze $originalName: " + result.result.messages.filter { it.isErrorOrHigher }.joinToString()}
                return ListView.of()
            }
        } catch (ex: Exception) {
            log.warn(ex) { "Failed to analyze $originalName" }
            ex.printStackTrace()
            return ListView.of()
        }

        // We pretty-print and parse the input resource again here, such that are sure
        // that the offset of the term is the same in the incomplete pretty-printed AST
        val pp = prettyPrint(ctx, ast)
        val ppResource = textResourceRegistry.createResource(pp)
        val ppAst = parseTask.runParse(ctx, ppResource.key)
        val explicatedAst = explicate(ctx, ppAst)

        // Get all possible incomplete ASTs
        val incompleteAsts = buildIncompleteAsts(explicatedAst)

        // Downgrade the placeholders in the incomplete ASTs, and pretty-print them
        val prettyPrintedAsts = incompleteAsts.mapNotNull { it.map { term ->
            prettyPrint(ctx, implicate(ctx, downgrade(ctx, term)))
        } }

        // Construct test cases and write the files to the test cases directory
        val testCases = mutableListOf<TestCase>()
        Files.createDirectories(input.testCaseDir.resolve(input.inputFile).parent)
        val indexedAsts = prettyPrintedAsts.withIndex()
        val sampledAsts = input.sample?.let { indexedAsts.sample(it, input.rnd) } ?: indexedAsts
        for((i, case) in ProgressBar.wrap(sampledAsts, "Input files")) {
            val name = input.inputFile.withName { "$it-$i" }.withExtension("").toString()

            log.trace { "Writing $name..." }
            // Write the pretty-printed AST to file
            val outputFile = input.testCaseDir.resolve(input.inputFile.withName { "$it-$i" })
            ctx.provide(outputFile)
            Files.writeString(outputFile, case.value)

            // Write the expected AST to file
            val expectedFile = outputFile.withName { "$it-expected" }.withExtension { "$it.aterm" }
            ctx.provide(expectedFile)
            termWriter.writeToPath(case.expectedAst, expectedFile)
//            Files.writeString(expectedFile, TermToString.toString(case.expectedAst))

            val placeholderRegion = getPlaceholderRegion(case.value)
            if (placeholderRegion == null) {
                log.warn { "Placeholder not found. Skipped $name." }
                continue
            }

            // Add the test case
            testCases.add(
                TestCase(
                    name,
                    input.inputFile,
                    input.testCaseDir.relativize(outputFile),
                    placeholderRegion.startOffset,
                    input.testCaseDir.relativize(expectedFile),
                )
            )
            log.debug { "Wrote $name." }
        }

        return ListView.of(testCases)
    }

    /**
     * Takes a term and produces all possible combinations of this term with a placeholder.
     *
     * @param term the term
     * @return a sequence of all possible variants of this term with a placeholder
     */
    private fun buildIncompleteAsts(term: IStrategoTerm): List<TestCaseInfo<IStrategoTerm>> {
        // Replaced the term with a placeholder
        return listOf(TestCaseInfo(makePlaceholder("x"), 0 /* getStartOffset(term)*/, term)) +
          // or replaced a subterm with all possible sub-asts with a placeholder
          term.subterms.flatMapIndexed { i, subTerm ->
              buildIncompleteAsts(subTerm).map { (newSubTerm, offset, expectedAst) -> TestCaseInfo(term.withSubterm(i, newSubTerm), offset, expectedAst) }
          }
    }

    /**
     * Replaces the subterm with the specified index in the term.
     *
     * @param index the zero-based index of the subterm to replace
     * @return the new term, with its subterm replaces
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T: IStrategoTerm> T.withSubterm(index: Int, term: IStrategoTerm): T {
        require(index in 0..this.subtermCount)
        val newSubterms = this.subterms.toTypedArray()
        newSubterms[index] = term
        return when (this) {
            is IStrategoAppl -> termFactory.makeAppl(this.constructor, newSubterms, this.annotations) as T
            is IStrategoList -> termFactory.makeList(newSubterms, this.annotations) as T
            is IStrategoTuple -> termFactory.makeTuple(newSubterms, this.annotations) as T
            else -> throw IllegalStateException("This should not happen.")
        }
    }

    // TODO: Don't use a heuristic
    private val placeholderRegex = Regex("\\[\\[\\w+\\]\\]")

    /**
     * Determines whether the given string contains a placeholder.
     */
    private fun hasPlaceholder(text: String): Boolean {
        // TODO: Don't use a heuristic
        return text.contains(placeholderRegex)
    }

    /**
     * Makes a placeholder.
     *
     * @param name the name of the placeholder, it can be anything
     * @return the created placeholder
     */
    private fun makePlaceholder(name: String): IStrategoPlaceholder {
        return termFactory.makePlaceholder(termFactory.makeString(name))
    }

    /**
     * Determines the region of a placeholder.
     *
     * @return the region of the placeholder; or `null` if not found
     */
    private fun getPlaceholderRegion(text: String): Region? {
        // TODO: Don't use a heuristic
        val range = placeholderRegex.find(text)?.range ?: return null
        return Region.fromOffsets(range.first, range.last + 1)
    }

    /**
     * Explicates the term.
     *
     * @param ctx the execution context
     * @param term the term
     * @return the downgraded term
     */
    private fun explicate(ctx: ExecContext, term: IStrategoTerm): IStrategoTerm {
        return ctx.require(explicateTask, Supplier { Result.ofOk<IStrategoTerm, Exception>(term) }).unwrap()
    }

    /**
     * Implicates the term.
     *
     * @param ctx the execution context
     * @param term the term
     * @return the downgraded term
     */
    private fun implicate(ctx: ExecContext, term: IStrategoTerm): IStrategoTerm {
        return ctx.require(implicateTask, Supplier { Result.ofOk<IStrategoTerm, Exception>(term) }).unwrap()
    }

    /**
     * Upgrades the placeholders in the term.
     *
     * @param ctx the execution context
     * @param term the term
     * @return the downgraded term
     */
    private fun upgrade(ctx: ExecContext, term: IStrategoTerm): IStrategoTerm {
        return ctx.require(upgradePlaceholdersTask, Supplier { Result.ofOk<IStrategoTerm, Exception>(term) }).unwrap()
    }

    /**
     * Downgrades the placeholders in the term.
     *
     * @param ctx the execution context
     * @param term the term
     * @return the downgraded term
     */
    private fun downgrade(ctx: ExecContext, term: IStrategoTerm): IStrategoTerm {
        return ctx.require(downgradePlaceholdersTask, Supplier { Result.ofOk<IStrategoTerm, Exception>(term) }).unwrap()
    }

    /**
     * Pretty-prints the term.
     *
     * @param ctx the execution context
     * @param term the term
     * @return the pretty-printed term
     */
    private fun prettyPrint(ctx: ExecContext, term: IStrategoTerm): String {
        return TermUtils.toJavaString(ctx.require(prettyPrintTask, Supplier { Result.ofOk<IStrategoTerm, Exception>(term) }).unwrap())
    }

    /**
     * Gets the start offset the specified term.
     *
     * @param term the term
     * @return the term's start offset
     */
    private fun getStartOffset(term: IStrategoTerm): Int = tryGetStartOffset(term)!!

    /**
     * Attempts to get the start offset the specified term.
     *
     * @param term the term
     * @return the term's start offset; or `null` when it could not be determined
     */
    private fun tryGetStartOffset(term: IStrategoTerm): Int? {
        val origin = TermOrigin.get(term).orElse(null) ?: return null
        val imploderAttachment = origin.imploderAttachment
        // We get the zero-based offset of the first character in the token
        return imploderAttachment.leftToken.startOffset
    }

    /**
     * A value and a placeholder offset.
     *
     * @property value the value
     * @property offset the placeholder offset
     * @property expectedAst the expected AST
     */
    data class TestCaseInfo<out T>(
        val value: T,
        val offset: Int,
        val expectedAst: IStrategoTerm,
    ) {
        fun <R> map(f: (T) -> R?): TestCaseInfo<R>? {
            val newValue = f(value) ?: return null
            return TestCaseInfo(newValue, offset, expectedAst)
        }
    }

}
