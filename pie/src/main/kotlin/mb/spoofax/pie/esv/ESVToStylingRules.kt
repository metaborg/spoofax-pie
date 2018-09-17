package mb.spoofax.pie.esv

import com.google.inject.Inject
import mb.log.api.Logger
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.config.ParseWorkspaceConfig
import mb.spoofax.pie.config.requireConfigValue
import mb.spoofax.pie.legacy.processOne
import mb.spoofax.runtime.cfg.LangId
import mb.spoofax.runtime.esv.StylingRulesFromESV
import mb.spoofax.runtime.style.StylingRules
import org.spoofax.interpreter.terms.IStrategoAppl
import java.io.Serializable

class ESVToStylingRules
@Inject constructor(
  logFactory: Logger,
  private val parseWorkspaceConfig: ParseWorkspaceConfig,
  private val stylingRulesFromESV: StylingRulesFromESV
) : TaskDef<ESVToStylingRules.Input, StylingRules?> {
  private val log: Logger = logFactory.forContext(ESVToStylingRules::class.java)

  companion object {
    const val id = "esv.ESVToStylingRules"
  }

  data class Input(
    val langId: LangId,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): StylingRules? {
    val (langId, root) = input
    val mainFile = requireConfigValue(this, parseWorkspaceConfig, root) { workspaceConfig ->
      workspaceConfig.langSpecConfigForId(langId)?.syntaxStyleFile()
    } ?: return null
    val ast = processOne(mainFile, log = log)?.ast ?: return null
    return stylingRulesFromESV.create(ast as IStrategoAppl)
  }
}