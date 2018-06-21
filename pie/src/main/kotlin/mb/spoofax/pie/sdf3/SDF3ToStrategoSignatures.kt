package mb.spoofax.pie.sdf3

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.config.ParseWorkspaceConfig
import mb.spoofax.pie.legacy.LegacyLoadProject
import mb.spoofax.pie.legacy.processAll
import mb.spoofax.runtime.sdf3.Signatures
import org.metaborg.core.action.EndNamedGoal
import java.io.Serializable

class SDF3ToStrategoSignatures
@Inject constructor(
  log: Logger,
  private val parseWorkspaceConfig: ParseWorkspaceConfig,
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

  data class LangSpecConfigInfo(
    val dir: PPath,
    val files: List<PPath>
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Signatures? {
    val (langSpecExt, root) = input

    val (langSpecDir, files) = with(parseWorkspaceConfig) {
      requireConfigValue(root) { workspaceConfig ->
        val langSpecConfig = workspaceConfig.langSpecConfigForExt(langSpecExt)
        if(langSpecConfig != null) {
          LangSpecConfigInfo(langSpecConfig.dir(), langSpecConfig.syntaxSignatureFiles())
        } else {
          null
        }
      }
    } ?: throw ExecException("Could not get language specification configuration for language $langSpecExt")

    val langSpecProject = require(legacyLoadProject, langSpecDir).v
    val outputs = processAll(files, langSpecProject, true, EndNamedGoal("Generate Signature (concrete)"), FileStampers.hash, FileStampers.modified, log)

    val signatureFiles = outputs
      .filter { it.ast != null && it.outputFile != null }
      .map { it.outputFile!! }
      .toCollection(ArrayList())
    val includeDir = langSpecDir.resolve("src-gen")
    return Signatures(signatureFiles, includeDir)
  }
}
