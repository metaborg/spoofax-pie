package mb.codecompletion.bench

import mb.common.result.Result
import mb.jsglr.pie.JsglrParseTaskDef
import mb.jsglr.pie.JsglrParseTaskInput
import mb.nabl2.terms.stratego.TermOrigin
import mb.pie.api.ExecContext
import mb.pie.api.None
import mb.pie.api.Supplier
import mb.pie.api.TaskDef
import mb.stratego.pie.AstStrategoTransformTaskDef
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
import kotlin.streams.asSequence

/**
 * Takes a file and produces n files, each with a placeholder at a different spot.
 * The comments at the top of the file indicate test information.
 */
class PrepareTestFileTask(
  val parseTask: JsglrParseTaskDef,
  val downgradePlaceholdersTask: AstStrategoTransformTaskDef,
  val prettyPrintTask: AstStrategoTransformTaskDef,
  val termFactory: ITermFactory,
) : TaskDef<PrepareTestFileTask.Input, None> {

  data class Input(
    val inputFile: Path,
    val outputDir: Path,
  ) : Serializable

  override fun getId(): String = CompletenessTest.TestTask::class.java.name

  override fun exec(ctx: ExecContext, input: Input): None {
    // Get the AST of the file
    val inputResource = ctx.require(input.inputFile)
    val jsglrResult = ctx.require(parseTask, JsglrParseTaskInput.builder()
        .withFile(inputResource.key)
        .build()
    ).unwrap()
    check(jsglrResult.ambiguous) { "${input.inputFile}: Parse result is ambiguous."}
    check(!jsglrResult.messages.containsErrorOrHigher()) { "${input.inputFile}: Parse result has errors: ${jsglrResult.messages.stream().asSequence().joinToString { "${it.region}: ${it.text}" }}"}
    val ast = jsglrResult.ast

    // Get all possible incomplete ASTs
    val incompleteAsts = buildIncompleteAsts(ast)

    // Downgrade the placeholders in the incomplete ASTs, and pretty-print them
    val prettyPrintedAsts = incompleteAsts.map { (ast, offset) -> WithOffset(prettyPrint(ctx, downgrade(ctx, ast)), offset) }

    // Prepend comments to the start
    val commentedAsts = prettyPrintedAsts.map { (astString, offset) ->
      "// OFFSET: $offset\n" +
      "// ---\n" +
      astString
    }

    // Write each pretty-printed AST to file
    ctx.provide(input.outputDir)
    Files.createDirectories(input.outputDir)
    for

    //prettyPrintedAsts =

//    parseTask.createAstSupplier()
    // Parse the time to AST
    // Replace each node of the AST with a placeholder
    // Downgrade the placeholders to placeholder terms
    // Pretty-print each AST to file
    // Append comments at the start
    TODO("Not yet implemented")
  }

  /**
   * Takes a term and produces all possible combinations of this term with a placeholder.
   *
   * @param term the term
   * @return a sequence of all possible variants of this term with a placeholder
   */
  private fun buildIncompleteAsts(term: IStrategoTerm): List<WithOffset<IStrategoTerm>> {
    // Replaced the term with a placeholder
    return listOf(WithOffset(makePlaceholder("x"), getStartOffset(term))) +
    // or replaced a subterm with all possible sub-asts with a placeholder
    term.subterms.flatMapIndexed { i, subTerm ->
      buildIncompleteAsts(subTerm).map { (newSubTerm, offset) -> WithOffset(term.withSubterm(i, newSubTerm), offset) }
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

//  /**
//   * Replaces the subterm with the specified index in the term.
//   *
//   * @param index the zero-based index of the subterm to replace
//   * @return the new term, with its subterm replaces
//   */
//  private fun IStrategoAppl.withSubterm(index: Int, term: IStrategoTerm): IStrategoAppl {
//    require(index in 0..this.subtermCount)
//    val newSubterms = this.subterms.toTypedArray()
//    newSubterms[index] = term
//    return termFactory.makeAppl(this.constructor, newSubterms, this.annotations)
//  }
//
//  /**
//   * Replaces the subterm with the specified index in the term.
//   *
//   * @param index the zero-based index of the subterm to replace
//   * @return the new term, with its subterm replaces
//   */
//  private fun IStrategoList.withSubterm(index: Int, term: IStrategoTerm): IStrategoList {
//    require(index in 0..this.subtermCount)
//    val newSubterms = this.subterms.toTypedArray()
//    newSubterms[index] = term
//    return termFactory.makeList(newSubterms, this.annotations)
//  }
//
//  /**
//   * Replaces the subterm with the specified index in the term.
//   *
//   * @param index the zero-based index of the subterm to replace
//   * @return the new term, with its subterm replaces
//   */
//  private fun IStrategoTuple.withSubterm(index: Int, term: IStrategoTerm): IStrategoTuple {
//    require(index in 0..this.subtermCount)
//    val newSubterms = this.subterms.toTypedArray()
//    newSubterms[index] = term
//    return termFactory.makeTuple(newSubterms, this.annotations)
//  }

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
   */
  data class WithOffset<out T>(
    val value: T,
    val offset: Int
  )
}
