package mb.spoofax.runtime.pie.esv

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.*
import mb.pie.runtime.stamp.FileStampers
import mb.spoofax.runtime.impl.esv.StylingRules
import mb.spoofax.runtime.impl.esv.StylingRulesFromESV
import mb.spoofax.runtime.pie.generated.createWorkspaceConfig
import mb.spoofax.runtime.pie.legacy.buildOrLoad
import mb.spoofax.runtime.pie.legacy.process
import mb.vfs.path.PPath
import org.spoofax.interpreter.terms.IStrategoAppl
import java.io.Serializable


class CompileStyler
@Inject constructor(
  log: Logger,
  private val stylingRulesFromESV: StylingRulesFromESV
) : TaskDef<CompileStyler.Input, StylingRules?> {
  private val log: Logger = log.forContext(CompileStyler::class.java)

  companion object {
    const val id = "CompileStyler"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): StylingRules? {
    val (langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class.java, createWorkspaceConfig.id, root)
        ?: throw ExecException("Could not create workspace config for root $root")
    val metaLangExt = "esv"
    val metaLangConfig = workspace.spxCoreConfigForExt(metaLangExt)
      ?: throw ExecException("Could not get meta-language config for extension $metaLangExt")
    val metaLangImpl = buildOrLoad(metaLangConfig)
    val langSpec =
      workspace.langSpecConfigForExt(input.langSpecExt)
        ?: throw ExecException("Could not get language specification config for extension $langSpecExt")
    val mainFile = langSpec.syntaxStyleFile() ?: return null
    val outputs = process(arrayListOf(mainFile), metaLangImpl, null, false, null, log)
    outputs.reqFiles.forEach { require(it, FileStampers.hash) }
    outputs.genFiles.forEach { generate(it, FileStampers.hash) }
    val ast = outputs.outputs.firstOrNull()?.ast ?: return null
    return stylingRulesFromESV.create(ast as IStrategoAppl)
  }
}