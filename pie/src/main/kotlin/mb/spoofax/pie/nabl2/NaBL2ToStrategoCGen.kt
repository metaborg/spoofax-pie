package mb.spoofax.pie.nabl2

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.generated.createWorkspaceConfig
import mb.spoofax.pie.legacy.*
import org.metaborg.core.action.CompileGoal
import java.io.Serializable

class NaBL2ToStrategoCGen
@Inject constructor(
  log: Logger,
  private val createWorkspaceConfig: createWorkspaceConfig,
  private val legacyBuildOrLoadLanguage: LegacyBuildOrLoadLanguage,
  private val legacyLoadProject: LegacyLoadProject
) : TaskDef<NaBL2ToStrategoCGen.Input, None> {
  private val log = log.forContext(NaBL2ToStrategoCGen::class.java)

  companion object {
    const val id = "nabl2.ToStrategoCGen"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): None {
    val (langSpecExt, root) = input

    // OPTO: only depend on Spoofax Core config for NaBL2, and language specification config for langSpecExt.
    val workspaceConfig = require(createWorkspaceConfig, root)
      ?: throw ExecException("Could not get workspace config at root $root")

    val metaLangExt = "nabl2"
    val metaLangConfig = workspaceConfig.spxCoreConfigForExt(metaLangExt)
      ?: throw ExecException("Could not get meta-language config for extension $metaLangExt")
    val metaLangImpl = require(legacyBuildOrLoadLanguage.createTask(metaLangConfig)).v

    // OPTO: only depend on natsNaBL2Files.
    val langSpec = workspaceConfig.langSpecConfigForExt(langSpecExt)
      ?: throw ExecException("Could not get language specification config for extension $langSpecExt")

    val langSpecProject = require(legacyLoadProject, langSpec.dir()).v

    val files = langSpec.natsNaBL2Files() ?: return None.instance
    val outputs = process(files, metaLangImpl, langSpecProject, true, CompileGoal(), log)
    outputs.reqFiles.forEach { require(it, FileStampers.hash) }
    outputs.genFiles.forEach { generate(it, FileStampers.hash) }

    return None.instance
  }
}