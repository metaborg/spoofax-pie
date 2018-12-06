package mb.spoofax.pie.nabl2

import com.google.inject.Inject
import mb.fs.java.JavaFSPath
import mb.log.api.Logger
import mb.pie.api.*
import mb.pie.api.fs.stamp.FileSystemStampers
import mb.spoofax.pie.config.ParseWorkspaceConfig
import mb.spoofax.pie.config.requireConfigValue
import mb.spoofax.pie.legacy.LegacyLoadProject
import mb.spoofax.pie.legacy.processAll
import mb.spoofax.runtime.cfg.LangId
import org.metaborg.core.action.CompileGoal
import java.io.Serializable

class NaBL2ToStrategoAnalyzer
@Inject constructor(
  log: Logger,
  private val parseWorkspaceConfig: ParseWorkspaceConfig,
  private val legacyLoadProject: LegacyLoadProject
) : TaskDef<NaBL2ToStrategoAnalyzer.Input, None> {
  private val log = log.forContext(NaBL2ToStrategoAnalyzer::class.java)

  companion object {
    const val id = "nabl2.ToStrategoAnalyzer"
  }

  data class Input(val langId: LangId, val root: JavaFSPath) : Serializable
  data class LangSpecConfigInfo(val dir: JavaFSPath, val files: ArrayList<JavaFSPath>) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): None {
    val (langId, root) = input

    val (langSpecDir, files) = requireConfigValue(this, parseWorkspaceConfig, root) { workspaceConfig ->
      val langSpecConfig = workspaceConfig.langSpecConfigForId(langId)
      if(langSpecConfig != null) {
        LangSpecConfigInfo(langSpecConfig.dir(), ArrayList(langSpecConfig.natsNaBL2Files()))
      } else {
        null
      }
    } ?: throw ExecException("Could not get language specification configuration for language with identifier $langId")

    val langSpecProject = require(legacyLoadProject, langSpecDir).v
    processAll(files.map { it.toNode() }, langSpecProject, true, CompileGoal(), FileSystemStampers.hash, FileSystemStampers.modified, log)

    return None.instance
  }
}