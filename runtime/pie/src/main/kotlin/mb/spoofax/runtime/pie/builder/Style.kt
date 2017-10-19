package mb.spoofax.runtime.pie.builder

import com.google.inject.Inject
import mb.pie.runtime.builtin.path.read
import mb.pie.runtime.core.BuildContext
import mb.pie.runtime.core.Builder
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.spoofax.runtime.impl.esv.*
import mb.spoofax.runtime.model.SpoofaxRunEx
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.model.style.Styling
import mb.spoofax.runtime.pie.builder.core.parse
import mb.vfs.path.PPath
import org.spoofax.interpreter.terms.IStrategoAppl
import java.io.Serializable

class GenerateStylerRules
@Inject constructor(private val stylingRulesFromESV: StylingRulesFromESV)
  : Builder<GenerateStylerRules.Input, StylingRules?> {
  companion object {
    val id = "spoofaxGenerateStylerRules"
  }

  data class Input(val esvLangConfig: SpxCoreConfig, val mainFile: PPath, val includedFiles: Iterable<PPath>) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): StylingRules? {
    val text = read(input.mainFile) ?: return null

    for(includedFile in input.includedFiles) {
      require(includedFile)
    }

    // Parse input file
    val (ast, _, _) = parse(input.esvLangConfig, text, input.mainFile)
    ast ?: throw SpoofaxRunEx("Main ESV file " + input.mainFile + " could not be parsed")

    val rules = stylingRulesFromESV.create(ast as IStrategoAppl)
    return rules
  }
}

class Style : Builder<Style.Input, Styling> {
  companion object {
    val id = "spoofaxStyle"
  }

  data class Input(val tokenStream: ArrayList<Token>, val rules: StylingRules) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Styling {
    val styler = Styler(input.rules)
    val styling = styler.style(input.tokenStream)
    return styling
  }
}
