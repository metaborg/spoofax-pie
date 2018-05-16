package mb.spoofax.runtime.pie.sdf3

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.spoofax.runtime.impl.sdf.Signatures
import mb.spoofax.runtime.pie.generated.createWorkspaceConfig
import mb.spoofax.runtime.pie.legacy.*
import mb.vfs.path.PPath
import mb.vfs.path.PathSrv
import org.metaborg.core.action.EndNamedGoal
import java.io.Serializable

class GenerateStrategoSignatures
@Inject constructor(
  log: Logger,
  private val pathSrv: PathSrv
) : TaskDef<GenerateStrategoSignatures.Input, Signatures?> {
  val log: Logger = log.forContext(GenerateStrategoSignatures::class.java)

  companion object {
    const val id = "sdf3.GenerateStrategoSignatures"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Signatures? {
    val (langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class.java, createWorkspaceConfig.id, root)
        ?: throw ExecException("Could not create workspace config for root $root")
    val metaLangExt = "sdf3"
    val metaLangConfig = workspace.spxCoreConfigForExt(metaLangExt)
      ?: throw ExecException("Could not get meta-language config for extension $metaLangExt")
    val metaLangImpl = buildOrLoad(metaLangConfig)
    val langSpec =
      workspace.langSpecConfigForExt(input.langSpecExt)
        ?: throw ExecException("Could not get language specification config for extension $langSpecExt")
    val langSpecProject = loadProj(langSpec.dir())
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