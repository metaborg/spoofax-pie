package mb.spoofax.pie.esv

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.generated.createWorkspaceConfig
import mb.spoofax.pie.legacy.*
import mb.spoofax.runtime.style.StylingRules
import mb.spoofax.runtime.esv.StylingRulesFromESV
import org.spoofax.interpreter.terms.IStrategoAppl
import java.io.Serializable

class ESVToStylingRules
@Inject constructor(
  log: Logger,
  private val stylingRulesFromESV: StylingRulesFromESV,
  private val createWorkspaceConfig: createWorkspaceConfig,
  private val legacyBuildOrLoadLanguage: LegacyBuildOrLoadLanguage
) : TaskDef<ESVToStylingRules.Input, StylingRules?> {
  private val log: Logger = log.forContext(ESVToStylingRules::class.java)

  companion object {
    const val id = "esv.ESVToStylingRules"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): StylingRules? {
    val (langSpecExt, root) = input

    val workspaceConfig = require(createWorkspaceConfig, root)
      ?: throw ExecException("Could not get workspace config at root $root")

    val metaLangExt = "esv"
    val metaLangConfig = workspaceConfig.spxCoreConfigForExt(metaLangExt)
      ?: throw ExecException("Could not get meta-language config for extension $metaLangExt")
    val metaLangImpl = require(legacyBuildOrLoadLanguage.createTask(metaLangConfig)).v

    val langSpec = workspaceConfig.langSpecConfigForExt(input.langSpecExt)
      ?: throw ExecException("Could not get language specification config for extension $langSpecExt")

    val mainFile = langSpec.syntaxStyleFile() ?: return null
    val outputs = process(arrayListOf(mainFile), metaLangImpl, null, false, null, log)
    outputs.reqFiles.forEach { require(it, FileStampers.hash) }
    outputs.genFiles.forEach { generate(it, FileStampers.hash) }
    val ast = outputs.outputs.firstOrNull()?.ast ?: return null
    return stylingRulesFromESV.create(ast as IStrategoAppl)
  }
}