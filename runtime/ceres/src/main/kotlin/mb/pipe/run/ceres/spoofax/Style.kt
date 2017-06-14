package mb.pipe.run.ceres.spoofax

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.Builder
import mb.pipe.run.ceres.path.cPath
import mb.pipe.run.ceres.path.read
import mb.pipe.run.ceres.spoofax.legacy.CoreParse
import mb.pipe.run.ceres.spoofax.legacy.CoreTrans
import mb.pipe.run.ceres.spoofax.legacy.loadLang
import mb.pipe.run.ceres.spoofax.legacy.loadProj
import mb.pipe.run.ceres.spoofax.legacy.parse
import mb.pipe.run.ceres.spoofax.legacy.trans
import mb.pipe.run.core.model.parse.Token
import mb.pipe.run.core.model.style.Styling
import mb.pipe.run.core.path.PPath
import mb.pipe.run.spoofax.esv.Styler
import mb.pipe.run.spoofax.esv.StylingRules
import mb.pipe.run.spoofax.esv.StylingRulesFromESV
import org.metaborg.core.action.CompileGoal
import org.spoofax.interpreter.terms.IStrategoAppl
import java.io.Serializable

class GenerateStylerRules
@Inject constructor(private val stylingRulesFromESV: StylingRulesFromESV)
  : Builder<GenerateStylerRules.Input, StylingRules> {
  data class Input(val langLoc: PPath, val specDir: PPath, val mainFile: PPath, val includedFiles: List<PPath>) : Serializable

  override val id: String = "generateStylerRules"
  override fun BuildContext.build(input: Input): StylingRules {
    val text = read(input.mainFile)

    for (includedFile in input.includedFiles) {
      require(includedFile.cPath)
    }

    // Load ESV, required for parsing, analysis, and transformation.
    val langImpl = loadLang(input.langLoc)
    val langId = langImpl.id()

    // Parse input file
    val ast = parse(CoreParse.Input(langId, input.mainFile, text)) ?: throw BuildException("Main ESV file " + input.mainFile + " could not be parsed")

    // Load project, required for analysis and transformation.
    loadProj(input.specDir)

    // Transform
    val output = trans(CoreTrans.Input(langId, input.specDir, input.mainFile, ast, CompileGoal()))
    if (output.ast == null) {
      throw BuildException("Main ESV file " + input.mainFile + " could not be compiled")
    }

    val rules = stylingRulesFromESV.create(output.ast as IStrategoAppl)
    return rules
  }
}

class Style : Builder<Style.Input, Styling> {
  data class Input(val tokenStream: List<Token>, val rules: StylingRules) : Serializable

  override val id = "spoofaxStyle"
  override fun BuildContext.build(input: Input): Styling {
    val styler = Styler(input.rules)
    val styling = styler.style(input.tokenStream)
    return styling
  }
}