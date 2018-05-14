package mb.spoofax.runtime.pie.nabl2

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.PathStampers
import mb.spoofax.runtime.pie.generated.createWorkspaceConfig
import mb.spoofax.runtime.pie.legacy.*
import mb.vfs.path.PPath
import org.metaborg.core.action.CompileGoal
import java.io.Serializable

class GenerateStrategoCGen
@Inject constructor(
  log: Logger
) : Func<GenerateStrategoCGen.Input, None> {
  private val log = log.forContext(GenerateStrategoCGen::class.java)

  companion object {
    const val id = "GenerateStrategoCGen"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): None {
    val (langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.id, root)
        ?: throw ExecException("Could not create workspace config for root $root")
    val metaLangExt = "nabl2"
    val metaLangConfig = workspace.spxCoreConfigForExt(metaLangExt)
      ?: throw ExecException("Could not get meta-language config for extension $metaLangExt")
    val metaLangImpl = buildOrLoad(metaLangConfig)
    val langSpec =
      workspace.langSpecConfigForExt(input.langSpecExt)
        ?: throw ExecException("Could not get language specification config for extension $langSpecExt")
    val langSpecProject = loadProj(langSpec.dir())
    val files = langSpec.natsNaBL2Files() ?: return None.instance
    val outputs = process(files, metaLangImpl, langSpecProject, true, CompileGoal(), log)
    outputs.reqFiles.forEach { require(it, PathStampers.hash) }
    outputs.genFiles.forEach { generate(it, PathStampers.hash) }

    return None.instance
  }
}