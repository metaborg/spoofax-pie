package mb.spoofax.pie.analysis

import com.google.inject.Inject
import mb.log.api.Logger
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.api.SpoofaxEx
import mb.spoofax.runtime.analysis.Analyzer
import mb.spoofax.runtime.cfg.LangId
import mb.spoofax.runtime.nabl.ScopeGraphPrimitiveLibrary
import mb.spoofax.runtime.stratego.StrategoRuntimeBuilder
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

class AnalyzeDocument @Inject constructor(
  log: Logger,
  private val compileAnalyzer: CompileAnalyzer,
  private val primitiveLibrary: ScopeGraphPrimitiveLibrary
) : TaskDef<AnalyzeDocument.Input, Analyzer.DocumentOutput?> {
  private val log: Logger = log.forContext(AnalyzeDocument::class.java)

  companion object {
    const val id = "analysis.Document"
  }

  data class Input(
    val document: PPath,
    val langId: LangId,
    val root: PPath,
    val ast: IStrategoTerm,
    val containerOutput: Analyzer.ContainerOutput
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Analyzer.DocumentOutput? {
    val (document, langId, root, ast, containerOutput) = input
    val analyzer = require(compileAnalyzer, CompileAnalyzer.Input(langId, root)) ?: return null
    val strategoRuntime = analyzer.createSuitableRuntime(StrategoRuntimeBuilder(), primitiveLibrary)
    require(analyzer.strategoCtree, FileStampers.hash)
    return try {
      analyzer.analyzeDocument(ast, document, containerOutput, strategoRuntime)
    } catch(e: SpoofaxEx) {
      log.error("Analyzing AST $ast of document $document failed", e)
      null
    }
  }
}
