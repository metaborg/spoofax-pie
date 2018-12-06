package mb.spoofax.pie.analysis

import com.google.inject.Inject
import mb.fs.java.JavaFSPath
import mb.log.api.Logger
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.api.fs.stamp.FileSystemStampers
import mb.spoofax.api.SpoofaxEx
import mb.spoofax.runtime.analysis.Analyzer
import mb.spoofax.runtime.cfg.LangId
import mb.spoofax.runtime.nabl.ScopeGraphPrimitiveLibrary
import mb.spoofax.runtime.stratego.StrategoRuntimeBuilder
import java.io.Serializable

class AnalyzeFinal @Inject constructor(
  log: Logger,
  private val compileAnalyzer: CompileAnalyzer,
  private val primitiveLibrary: ScopeGraphPrimitiveLibrary
) : TaskDef<AnalyzeFinal.Input, Analyzer.FinalOutput?> {
  private val log: Logger = log.forContext(AnalyzeFinal::class.java)

  companion object {
    const val id = "analysis.Final"
  }

  data class Input(
    val langId: LangId,
    val root: JavaFSPath,
    val containerOutput: Analyzer.ContainerOutput,
    val documentOutputs: ArrayList<Analyzer.DocumentOutput>
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Analyzer.FinalOutput? {
    val (langId, root, containerOutput, documentOutputs) = input
    val analyzer = require(compileAnalyzer, CompileAnalyzer.Input(langId, root)) ?: return null
    val strategoRuntime = analyzer.createSuitableRuntime(StrategoRuntimeBuilder(), primitiveLibrary)
    require(analyzer.strategoCtree, FileSystemStampers.hash)
    return try {
      analyzer.analyzeFinal(containerOutput, documentOutputs, strategoRuntime)
    } catch(e: SpoofaxEx) {
      log.error("Finalizing analysis failed", e)
      null
    }
  }
}
