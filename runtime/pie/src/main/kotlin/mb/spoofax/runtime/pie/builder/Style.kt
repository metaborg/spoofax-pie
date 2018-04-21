package mb.spoofax.runtime.pie.builder

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.PathStampers
import mb.spoofax.runtime.impl.esv.*
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.model.style.Styling
import mb.spoofax.runtime.pie.builder.core.buildOrLoad
import mb.spoofax.runtime.pie.builder.core.process
import mb.spoofax.runtime.pie.generated.createWorkspaceConfig
import mb.vfs.path.PPath
import org.spoofax.interpreter.terms.IStrategoAppl
import java.io.Serializable

class GenerateStylerRules
@Inject constructor(
  log: Logger,
  private val stylingRulesFromESV: StylingRulesFromESV
) : Func<GenerateStylerRules.Input, StylingRules?> {
  val log: Logger = log.forContext(GenerateStylerRules::class.java)

  companion object {
    val id = "spoofaxGenerateStylerRules"
  }

  data class Input(
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): StylingRules? {
    val (langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class, createWorkspaceConfig.Companion.id, root)
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
    outputs.reqFiles.forEach { require(it, PathStampers.hash) }
    outputs.genFiles.forEach { generate(it, PathStampers.hash) }
    val ast = outputs.outputs.firstOrNull()?.ast ?: return null
    val rules = stylingRulesFromESV.create(ast as IStrategoAppl)
    return rules
  }
}

class Style : Func<Style.Input, Styling> {
  companion object {
    val id = "spoofaxStyle"
  }

  data class Input(
    val tokenStream: ArrayList<Token>,
    val rules: StylingRules
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Styling {
    val styler = Styler(input.rules)
    val styling = styler.style(input.tokenStream)
    return styling
  }
}
