package mb.pipe.run.ceres.spoofax

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.Builder
import mb.pipe.run.ceres.path.cPath
import mb.pipe.run.ceres.path.read
import mb.pipe.run.ceres.spoofax.core.parse
import mb.pipe.run.core.PipeRunEx
import mb.pipe.run.core.model.parse.Token
import mb.pipe.run.core.model.style.Styling
import mb.pipe.run.core.path.PPath
import mb.pipe.run.spoofax.cfg.SpxCoreConfig
import mb.pipe.run.spoofax.esv.Styler
import mb.pipe.run.spoofax.esv.StylingRules
import mb.pipe.run.spoofax.esv.StylingRulesFromESV
import org.spoofax.interpreter.terms.IStrategoAppl
import java.io.Serializable

class GenerateStylerRules
@Inject constructor(private val stylingRulesFromESV: StylingRulesFromESV)
  : Builder<GenerateStylerRules.Input, StylingRules> {
  companion object {
    val id = "spoofaxGenerateStylerRules"
  }

  data class Input(val esvLangConfig: SpxCoreConfig, val mainFile: PPath, val includedFiles: ArrayList<PPath>) : Serializable

  override val id = Companion.id
  override fun BuildContext.build(input: Input): StylingRules {
    val text = read(input.mainFile)

    for (includedFile in input.includedFiles) {
      require(includedFile.cPath)
    }

    // Parse input file
    val (ast, _, _) = parse(input.esvLangConfig, text)
    ast ?: throw PipeRunEx("Main ESV file " + input.mainFile + " could not be parsed")

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
