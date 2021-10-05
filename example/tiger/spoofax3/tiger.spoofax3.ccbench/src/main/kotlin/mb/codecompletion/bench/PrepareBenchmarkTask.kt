package mb.codecompletion.bench

import mb.aterm.common.TermToString
import mb.codecompletion.bench.utils.runParse
import mb.codecompletion.bench.utils.withExtension
import mb.codecompletion.bench.utils.withName
import mb.common.result.Result
import mb.common.util.ListView
import mb.nabl2.terms.stratego.TermOrigin
import mb.pie.api.ExecContext
import mb.pie.api.Pie
import mb.pie.api.Supplier
import mb.pie.api.TaskDef
import mb.resource.text.TextResourceRegistry
import mb.tiger.task.TigerDowngradePlaceholdersStatix
import mb.tiger.task.TigerPPPartial
import mb.tiger.task.TigerParse
import org.spoofax.interpreter.terms.IStrategoAppl
import org.spoofax.interpreter.terms.IStrategoList
import org.spoofax.interpreter.terms.IStrategoPlaceholder
import org.spoofax.interpreter.terms.IStrategoTerm
import org.spoofax.interpreter.terms.IStrategoTuple
import org.spoofax.interpreter.terms.ITermFactory
import org.spoofax.terms.util.TermUtils
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

/**
 * Runs a single benchmark.
 */
class PrepareBenchmarkTask @Inject constructor(
    private val parseTask: TigerParse,
    private val downgradePlaceholdersTask: TigerDowngradePlaceholdersStatix,
    private val prettyPrintTask: TigerPPPartial,
    private val textResourceRegistry: TextResourceRegistry,
    private val termFactory: ITermFactory,
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
    ): Serializable

    /**
     * Runs this task.
     *
     * @param inputFile the task input file
     * @return the benchmark
     */
    fun run(pie: Pie, projectDir: Path, inputFile: Path, testCaseDir: Path): List<TestCase> {
        pie.newSession().use { session ->
            val topDownSession = session.updateAffectedBy(emptySet())
            return topDownSession.require(this.createTask(Input(projectDir, inputFile, testCaseDir))).toList()
        }
    }

    override fun getId(): String = PrepareBenchmarkTask::class.java.name

    override fun exec(ctx: ExecContext, input: Input): ListView<TestCase> {
        // Get the AST of the file
        // We parse and pretty-print the input resource here, such that are sure
        // that the offset of the term is the same in the incomplete pretty-printed AST
        val resInputFile = input.projectDir.resolve(input.inputFile)
        val inputResource = ctx.require(resInputFile)
        val ast = parseTask.runParse(ctx, inputResource.key)
        val pp = prettyPrint(ctx, ast)
        val ppResource = textResourceRegistry.createResource(pp)
        val ppAst = parseTask.runParse(ctx, ppResource.key)

        // Get all possible incomplete ASTs
        val incompleteAsts = buildIncompleteAsts(ppAst)

        // Downgrade the placeholders in the incomplete ASTs, and pretty-print them
        val prettyPrintedAsts = incompleteAsts.mapNotNull { it.map { term ->
            prettyPrint(ctx, downgrade(ctx, term))
        } }

        // Construct test cases and write the files to the test cases directory
        val testCases = mutableListOf<TestCase>()
        Files.createDirectories(input.testCaseDir.resolve(input.inputFile).parent)
        for((i, case) in prettyPrintedAsts.withIndex()) {
            val name = input.inputFile.withName { "$it-$i" }.withExtension("").toString()

            println("Writing $name...")
            // Write the pretty-printed AST to file
            val outputFile = input.testCaseDir.resolve(input.inputFile.withName { "$it-$i" })
            ctx.provide(outputFile)
            Files.writeString(outputFile, case.value)

            // Write the expected AST to file
            val expectedFile = outputFile.withName { "$it-expected" }.withExtension { "$it.aterm" }
            ctx.provide(expectedFile)
            Files.writeString(expectedFile, TermToString.toString(case.expectedAst))

            if (!hasPlaceholder(case.value)) {
                println("Skipped $name.")
                continue
            }

            // Add the test case
            testCases.add(
                TestCase(
                    name,
                    input.inputFile,
                    input.testCaseDir.relativize(outputFile),
                    case.offset,
                    input.testCaseDir.relativize(expectedFile),
                )
            )
            println("Wrote $name.")
        }

        return ListView.of(testCases)
    }

    /**
     * Determines whether the given string contains a placeholder.
     */
    private fun hasPlaceholder(text: String): Boolean {
        return text.contains(Regex("\\[\\[[^\\]]+\\]\\]"))
    }


    /**
     * Takes a term and produces all possible combinations of this term with a placeholder.
     *
     * @param term the term
     * @return a sequence of all possible variants of this term with a placeholder
     */
    private fun buildIncompleteAsts(term: IStrategoTerm): List<TestCaseInfo<IStrategoTerm>> {
        // Replaced the term with a placeholder
        return listOf(TestCaseInfo(makePlaceholder("x"), getStartOffset(term), term)) +
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
            else -> TODO("This should not happen.")
        }
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
