package mb.codecompletion.bench

import mb.aterm.common.TermToString
import mb.codecompletion.bench.utils.runParse
import mb.common.result.Result
import mb.common.util.ListView
import mb.nabl2.terms.stratego.TermOrigin
import mb.pie.api.ExecContext
import mb.pie.api.None
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
import java.nio.file.Paths
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
) : TaskDef<String, ListView<Benchmark>> {

    /**
     * Runs this task.
     *
     * @param inputFile the task input file
     * @return the benchmark
     */
    fun run(pie: Pie, inputFile: Path): List<Benchmark> {
        pie.newSession().use { session ->
            val topDownSession = session.updateAffectedBy(emptySet())
            return topDownSession.require(this.createTask(inputFile.toString())).toList()
        }
    }

    override fun getId(): String = PrepareBenchmarkTask::class.java.name

    override fun exec(ctx: ExecContext, inputFile: String): ListView<Benchmark> {
        val inputFilePath = Path.of(inputFile)
        // Get the AST of the file
        // We parse the input resource here, such that we don't measure the overhead of parsing the input resource again
        val inputResource = ctx.require(inputFilePath)
        val ast = parseTask.runParse(ctx, inputResource.key)
        val pp = prettyPrint(ctx, ast)
        val ppResource = textResourceRegistry.createResource(pp)
        val ppAst = parseTask.runParse(ctx, ppResource.key)

        // Get all possible incomplete ASTs
        val incompleteAsts = buildIncompleteAsts(ppAst)

        // Downgrade the placeholders in the incomplete ASTs, and pretty-print them
        val prettyPrintedAsts = incompleteAsts.map { it.map { term -> prettyPrint(ctx, downgrade(ctx, term)) } }

        // Append comments to the end
//        val relativePath = input.projectDir.relativize(input.inputFile)
//        val relativeDir = relativePath.parent ?: Paths.get("./")
//        val filename = PrepareTestFileTask.Filename.parse(relativePath.fileName.toString())
//        val commentedTestCases = prettyPrintedAsts.map { case -> case.map {
//            it + "\n${
//                CodeCompleteBenchTask.TestHeaders(
//                    fileName = relativePath,
//                    offset = case.offset,
//                )
//            }"
//        } }

        return ListView.of(prettyPrintedAsts.map {
            Benchmark(
                inputFilePath.fileName.toString(),
                it.value, it.offset, it.expectedAst
            )
        })

//        ctx.provide(input.outputDir)
//        Files.createDirectories(input.outputDir)
//        for((i, commentedTestCase) in prettyPrintedAsts.withIndex()) {
//            // Write the pretty-printed AST to file
//            val filePath = input.outputDir.resolve(relativeDir).resolve(filename.copy(name = "${filename.name}-$i").toString())
//            ctx.provide(filePath)
//            Files.writeString(filePath, commentedTestCase.value)
//
//            // Write the expected AST to file
//            val expectedFilePath = input.outputDir.resolve(relativeDir).resolve(filename.copy(name = "${filename.name}-$i-expected", extension = filename.extension + ".aterm").toString())
//            ctx.provide(expectedFilePath)
//            Files.writeString(expectedFilePath, TermToString.toString(commentedTestCase.expectedAst))
//        }
//        return Benchmark(
//            input.inputFile.fileName.toString(),
//
//        )
//        TODO()
    }


    /**
     * Takes a term and produces all possible combinations of this term with a placeholder.
     *
     * @param term the term
     * @return a sequence of all possible variants of this term with a placeholder
     */
    private fun buildIncompleteAsts(term: IStrategoTerm): List<TestCase<IStrategoTerm>> {
        // Replaced the term with a placeholder
        return listOf(TestCase(makePlaceholder("x"), getStartOffset(term), term)) +
          // or replaced a subterm with all possible sub-asts with a placeholder
          term.subterms.flatMapIndexed { i, subTerm ->
              buildIncompleteAsts(subTerm).map { (newSubTerm, offset, expectedAst) -> TestCase(term.withSubterm(i, newSubTerm), offset, expectedAst) }
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
    data class TestCase<out T>(
        val value: T,
        val offset: Int,
        val expectedAst: IStrategoTerm,
    ) {
        fun <R> map(f: (T) -> R): TestCase<R> {
            return TestCase(f(value), offset, expectedAst)
        }
    }

}
