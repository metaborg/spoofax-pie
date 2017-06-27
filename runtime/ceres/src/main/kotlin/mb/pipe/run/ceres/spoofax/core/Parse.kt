package mb.pipe.run.ceres.spoofax.core

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.Builder
import mb.ceres.PathStampers
import mb.pipe.run.ceres.util.Tuple3
import mb.pipe.run.core.log.Logger
import mb.pipe.run.core.model.message.Msg
import mb.pipe.run.core.model.parse.Token
import mb.pipe.run.core.path.PPath
import mb.pipe.run.spoofax.sdf.TokenExtractor
import mb.pipe.run.spoofax.util.MessageConverter
import org.metaborg.core.language.LanguageIdentifier
import org.metaborg.core.syntax.ParseException
import org.metaborg.spoofax.core.syntax.SyntaxFacet
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

class CoreParse @Inject constructor(log: Logger, val messageConverter: MessageConverter) : Builder<CoreParse.Input, CoreParse.Output> {
  companion object {
    val id = "coreParse"
  }

  data class Input(val langId: LanguageIdentifier, val file: PPath, val text: String) : Serializable
  data class Output(val ast: IStrategoTerm?, val tokens: ArrayList<Token>?, val messages: ArrayList<Msg>) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Output {
    val spoofax = Spx.spoofax()
    val langImpl = spoofax.languageService.getImpl(input.langId) ?: throw BuildException("Language with id ${input.langId} does not exist or has not been loaded into Spoofax core")

    // Require parse table
    val facet = langImpl.facet<SyntaxFacet>(SyntaxFacet::class.java)
    if (facet != null) {
      val parseTableFile = facet.parseTable
      if (parseTableFile != null) {
        require(parseTableFile.cPath, PathStampers.hash)
      }
    }

    // Perform parsing
    val resource = input.file.fileObject
    val inputUnit = spoofax.unitService.inputUnit(resource, input.text, langImpl, null)
    try {
      val parseUnit = spoofax.syntaxService.parse(inputUnit)
      val ast = parseUnit.ast();
      val tokens = if (ast != null) TokenExtractor.extract(ast) else null
      val messages = messageConverter.toMsgs(parseUnit.messages());
      return Output(ast, tokens, messages);
    } catch(e: ParseException) {
      log.error("Parsing failed unexpectedly", e)
      return Output(null, null, ArrayList(0))
    }
  }
}

fun BuildContext.parse(input: CoreParse.Input) = requireOutput(CoreParse::class.java, input)