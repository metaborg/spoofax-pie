package mb.spoofax.pie.sdf3

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
    val langId: LangId,
    val root: JavaFSPath
  ) : Serializable

  data class LangSpecConfigInfo(
    val dir: JavaFSPath,
    val files: List<JavaFSPath>
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Signatures? {
    val (langId, root) = input

    val (langSpecDir, files) = requireConfigValue(this, parseWorkspaceConfig, root) { workspaceConfig ->
      val langSpecConfig = workspaceConfig.langSpecConfigForId(langId)
      if(langSpecConfig != null) {
        LangSpecConfigInfo(langSpecConfig.dir(), langSpecConfig.syntaxSignatureFiles())
      } else {
        null
      }
    } ?: throw ExecException("Could not get language specification configuration for language with identifier $langId")

    val langSpecProject = require(legacyLoadProject, langSpecDir).v
    val outputs = processAll(files.map { it.toNode() }, langSpecProject, true, EndNamedGoal("Generate Signature (concrete)"), FileSystemStampers.hash, FileSystemStampers.modified, log)

    val signatureFiles = outputs
      .filter { it.ast != null && it.outputFile != null }
      .map { it.outputFile!! }
      .toCollection(ArrayList())
    val includeDir = langSpecDir.appendSegment("src-gen")
    return Signatures(signatureFiles.mapTo(ArrayList()) { it.path }, includeDir)
  }
}
