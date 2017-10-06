package mb.spoofax.runtime.pie.builder.core

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.builtin.util.Tuple3
import mb.pie.runtime.core.*
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.spoofax.runtime.impl.legacy.MessageConverter
import mb.spoofax.runtime.impl.sdf.TokenExtractor
import mb.spoofax.runtime.model.message.Msg
import mb.spoofax.runtime.model.parse.Token
import org.metaborg.core.syntax.ParseException
import org.metaborg.spoofax.core.syntax.SyntaxFacet
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

class CoreParse @Inject constructor(log: Logger, private val messageConverter: MessageConverter) : Builder<CoreParse.Input, CoreParse.Output> {
  companion object {
    val id = "coreParse"
  }

  data class Input(val config: SpxCoreConfig, val text: String) : Serializable
  data class Output(val ast: IStrategoTerm?, val tokens: ArrayList<Token>?, val messages: ArrayList<Msg>) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): Output {
    val spoofax = Spx.spoofax()
    val langImpl = buildOrLoad(input.config)

    // Require parse table
    val facet = langImpl.facet<SyntaxFacet>(SyntaxFacet::class.java)
    if(facet != null) {
      val parseTableFile = facet.parseTable
      if(parseTableFile != null) {
        require(parseTableFile.pPath, PathStampers.hash)
      }
    }

    // Perform parsing
    val inputUnit = spoofax.unitService.inputUnit(input.text, langImpl, null)
    try {
      val parseUnit = spoofax.syntaxService.parse(inputUnit)
      val ast = parseUnit.ast();
      val tokens = if(ast != null) TokenExtractor.extract(ast) else null
      val messages = messageConverter.toMsgs(parseUnit.messages());
      return Output(ast, tokens, messages)
    } catch(e: ParseException) {
      log.error("Parsing failed unexpectedly", e)
      return Output(null, null, ArrayList(0))
    }
  }
}

fun BuildContext.parse(input: CoreParse.Input) = requireOutput(CoreParse::class.java, input)
fun BuildContext.parse(config: SpxCoreConfig, text: String) = parse(CoreParse.Input(config, text))
