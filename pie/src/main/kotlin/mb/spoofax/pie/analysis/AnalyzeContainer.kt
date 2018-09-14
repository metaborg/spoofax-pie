package mb.spoofax.pie.analysis

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.api.SpoofaxEx
import mb.spoofax.runtime.analysis.Analyzer
import mb.spoofax.runtime.cfg.LangId
import mb.spoofax.runtime.nabl.ScopeGraphPrimitiveLibrary
import mb.spoofax.runtime.stratego.StrategoRuntimeBuilder
import java.io.Serializable

class AnalyzeContainer @Inject constructor(
  log: Logger,
  private val compileAnalyzer: CompileAnalyzer,
  private val primitiveLibrary: ScopeGraphPrimitiveLibrary
) : TaskDef<AnalyzeContainer.Input, Analyzer.ContainerOutput?> {
  private val log: Logger = log.forContext(AnalyzeContainer::class.java)

  companion object {
    const val id = "analysis.Container"
  }

  data class Input(
    val container: PPath,
    val langId: LangId,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Analyzer.ContainerOutput? {
    val (container, langId, root) = input
    val analyzer = require(compileAnalyzer, CompileAnalyzer.Input(langId, root)) ?: return null
    val strategoRuntime = analyzer.createSuitableRuntime(StrategoRuntimeBuilder(), primitiveLibrary)
    require(analyzer.strategoCtree, FileStampers.hash)
    return try {
      analyzer.analyzeContainer(container, strategoRuntime)
    } catch(e: SpoofaxEx) {
      log.error("Analyzing container $container failed", e)
      null
    }
  }
}
