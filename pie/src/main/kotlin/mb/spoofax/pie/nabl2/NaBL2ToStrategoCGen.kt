package mb.spoofax.pie.nabl2

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.config.ParseWorkspaceConfig
import mb.spoofax.pie.legacy.LegacyLoadProject
import mb.spoofax.pie.legacy.processAll
import org.metaborg.core.action.CompileGoal
import java.io.Serializable

class NaBL2ToStrategoCGen
@Inject constructor(
  log: Logger,
  private val parseWorkspaceConfig: ParseWorkspaceConfig,
  private val legacyLoadProject: LegacyLoadProject
) : TaskDef<NaBL2ToStrategoCGen.Input, None> {
  private val log = log.forContext(NaBL2ToStrategoCGen::class.java)

  companion object {
    const val id = "nabl2.ToStrategoCGen"
  }

  data class Input(val langSpecExt: String, val root: PPath) : Serializable
  data class LangSpecConfigInfo(val dir: PPath, val files: ArrayList<PPath>) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): None {
    val (langSpecExt, root) = input

    val (langSpecDir, files) = with(parseWorkspaceConfig) {
      requireConfigValue(root) { workspaceConfig ->
        val langSpecConfig = workspaceConfig.langSpecConfigForExt(langSpecExt)
        if(langSpecConfig != null) {
          LangSpecConfigInfo(langSpecConfig.dir(), ArrayList(langSpecConfig.natsNaBL2Files()))
        } else {
          null
        }
      }
    } ?: throw ExecException("Could not get language specification configuration for language $langSpecExt")

    val langSpecProject = require(legacyLoadProject, langSpecDir).v
    processAll(files, langSpecProject, true, CompileGoal(), FileStampers.hash, FileStampers.modified, log)

    return None.instance
  }
}