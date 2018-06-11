package mb.spoofax.pie.sdf3

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.generated.createWorkspaceConfig
import mb.spoofax.pie.legacy.*
import mb.spoofax.runtime.sdf3.Signatures
import org.metaborg.core.action.EndNamedGoal
import java.io.Serializable

class SDF3ToStrategoSignatures
@Inject constructor(
  log: Logger,
  private val createWorkspaceConfig: createWorkspaceConfig,
  private val legacyBuildOrLoadLanguage: LegacyBuildOrLoadLanguage,
  private val legacyLoadProject: LegacyLoadProject
) : TaskDef<SDF3ToStrategoSignatures.Input, Signatures?> {
  val log: Logger = log.forContext(SDF3ToStrategoSignatures::class.java)

  companion object {
    const val id = "sdf3.ToStrategoSignatures"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Signatures? {
    val (langSpecExt, root) = input

    // OPTO: only depend on Spoofax Core config for SDF3, and language specification config for langSpecExt.
    val workspaceConfig = require(createWorkspaceConfig, root)
      ?: throw ExecException("Could not get workspace config at root $root")

    val metaLangExt = "sdf3"
    val metaLangConfig = workspaceConfig.spxCoreConfigForExt(metaLangExt)
      ?: throw ExecException("Could not get meta-language config for extension $metaLangExt")
    val metaLangImpl = require(legacyBuildOrLoadLanguage.createTask(metaLangConfig)).v

    // OPTO: only depend on syntax signature files.
    val langSpec = workspaceConfig.langSpecConfigForExt(langSpecExt)
      ?: throw ExecException("Could not get language specification config for extension $langSpecExt")

    val langSpecProject = require(legacyLoadProject, langSpec.dir()).v

    val files = langSpec.syntaxSignatureFiles() ?: return null
    val outputs = process(files, metaLangImpl, langSpecProject, true, EndNamedGoal("Generate Signature (concrete)"), log)
    outputs.reqFiles.forEach { require(it, FileStampers.hash) }
    outputs.genFiles.forEach { generate(it, FileStampers.hash) }

    val signatureFiles = outputs.outputs
      .filter { it.ast != null && it.outputFile != null }
      .map { it.outputFile!! }
      .toCollection(ArrayList())
    val includeDir = langSpec.dir().resolve("src-gen")
    return Signatures(signatureFiles, includeDir)
  }
}