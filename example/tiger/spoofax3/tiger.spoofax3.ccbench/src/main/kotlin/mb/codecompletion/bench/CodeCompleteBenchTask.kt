package mb.codecompletion.bench

import mb.common.region.Region
import mb.jsglr.pie.JsglrParseTaskInput
import mb.nabl2.terms.stratego.StrategoTerms
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.resource.ReadableResource
import mb.statix.TermCodeCompletionItem
import mb.statix.TermCodeCompletionResult
import mb.statix.codecompletion.pie.CodeCompletionTaskDef
import mb.statix.codecompletion.pie.MeasuringCodeCompletionEventHandler
import mb.tiger.task.TigerCodeCompletionTaskDef
import mb.tiger.task.TigerParse
import org.spoofax.interpreter.terms.IStrategoTerm
import org.spoofax.interpreter.terms.ITermFactory
import org.spoofax.terms.io.binary.TermReader
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Serializable
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.streams.asSequence

/**
 * Benchmarks a single code completion invocation.
 */
@Deprecated("")
class CodeCompleteBenchTask @Inject constructor(
  val parseTask: TigerParse,
  val codeCompletionTask: TigerCodeCompletionTaskDef,
  val termFactory: ITermFactory,
) : TaskDef<CodeCompleteBenchTask.Input, CodeCompleteBenchResult> {

  data class Input(
    val projectDir: Path,
    val inputFile: Path,
  ) : Serializable

  override fun getId(): String = CodeCompleteBenchTask::class.java.name

  override fun exec(ctx: ExecContext, input: Input): CodeCompleteBenchResult {
    val inputFile = input.inputFile
    val expectedAstFile = input.inputFile.resolveSibling(input.inputFile.nameWithoutExtension + "-expected." + input.inputFile.extension + ".aterm")
    val ast = parse(ctx, inputFile)
    val projectDirResource = ctx.require(input.projectDir)
    val inputResource = ctx.require(inputFile)
    val headers = readHeaders(inputResource)
    ctx.require(expectedAstFile)
    val expectedTerm = StrategoTerms(termFactory).fromStratego(TermReader(termFactory).parseFromFile(expectedAstFile.toString()))
    val eventHandler = MeasuringCodeCompletionEventHandler()

    val results = ctx.require(
      codeCompletionTask, CodeCompletionTaskDef.Input(
        Region.atOffset(headers.offset),
        inputResource.key,
        projectDirResource.path,
        eventHandler,
      )
    ).unwrap() as TermCodeCompletionResult

    val matches = results.proposals.filterIsInstance<TermCodeCompletionItem>().filter { tryMatchExpectation(results.placeholder, expectedTerm, it.term) }
    if (matches.isEmpty()) {
      throw IllegalStateException("Test failed, nothing matched expected AST: $expectedTerm\nGot: " + results.proposals.joinToString())
    }

    return CodeCompleteBenchResult(
      eventHandler.parseTime,
      eventHandler.preparationTime,
      eventHandler.analysisTime,
      eventHandler.codeCompletionTime,
      eventHandler.finishingTime,
      eventHandler.totalTime,
      0,
      0,
      0,
      0,
        true,
      results.proposals.toList()
    )
  }

  private fun parse(ctx: ExecContext, inputFile: Path): IStrategoTerm {
    val inputResource = ctx.require(inputFile)
    val jsglrResult = ctx.require(parseTask, JsglrParseTaskInput.builder()
      .withFile(inputResource.key)
      .build()
    ).unwrap()
    check(!jsglrResult.ambiguous) { "${inputFile}: Parse result is ambiguous."}
    check(!jsglrResult.messages.containsErrorOrHigher()) { "${inputFile}: Parse result has errors: ${jsglrResult.messages.stream().asSequence().joinToString { "${it.region}: ${it.text}" }}"}
    return jsglrResult.ast
  }

  private fun readHeaders(resource: ReadableResource): TestHeaders {
    val headerLines = mutableListOf<String>()
    resource.openReadBuffered().use { BufferedReader(InputStreamReader(it)).use { reader ->
      var line = reader.readLine()
      while (line != null && !line.startsWith("/// ---")) {
        line = reader.readLine()
      }
      if (line != null) {
        line = reader.readLine()
        while (line != null && line.startsWith("///")) {
          headerLines.add(line.substring(3).trim());
          line = reader.readLine()
        }
      }
    } }
    return TestHeaders.parse(headerLines)
  }

  data class TestHeaders(
    val fileName: Path,
    val offset: Int,
  ) {
    companion object {
      fun parse(headerLines: List<String>): TestHeaders {
        val fileName = headerLines.mapNotNull { tryRead(it, "FILENAME") }.map { Paths.get(it) }.firstOrNull() ?: throw IllegalArgumentException("Filename not specified.")
        val offset = headerLines.mapNotNull { tryRead(it, "OFFSET") }.map { it.toInt() }.firstOrNull() ?: throw IllegalArgumentException("Offset not specified.")
        return TestHeaders(fileName, offset)
      }

      fun tryRead(headerLine: String, fieldName: String): String? {
        return headerLine.takeIf { it.startsWith("$fieldName:", true) }?.substring(fieldName.length + 1)?.trim()
      }
    }

    override fun toString(): String {
      return """
        /// ---
        /// FILENAME: $fileName
        /// OFFSET: $offset
        """.trimIndent()
    }
  }
}
