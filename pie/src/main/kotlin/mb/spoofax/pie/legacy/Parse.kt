package mb.spoofax.pie.legacy

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.lang.runtime.util.Tuple2
import mb.pie.lang.runtime.util.Tuple3
import mb.pie.vfs.path.PPath
import mb.spoofax.api.message.Msg
import mb.spoofax.api.parse.Token
import mb.spoofax.legacy.MessageConverter
import mb.spoofax.runtime.cfg.SpxCoreConfig
import mb.spoofax.runtime.sdf.TokenExtractor
import org.metaborg.core.syntax.ParseException
import org.metaborg.spoofax.core.syntax.SyntaxFacet
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

class LegacyParse @Inject constructor(
  log: Logger,
  private val messageConverter: MessageConverter,
  private val legacyBuildOrLoadLanguage: LegacyBuildOrLoadLanguage
) : TaskDef<LegacyParse.Input, LegacyParse.Output> {
  private val log: Logger = log.forContext(LegacyParse::class.java)

  companion object {
    const val id = "legacy.Parse"
  }

  data class Input(val config: SpxCoreConfig, val text: String, val file: PPath) : Serializable
  data class Output(val ast: IStrategoTerm?, val tokens: ArrayList<Token>?, val messages: ArrayList<Msg>, val file: PPath) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  override val id = Companion.id
  override fun key(input: Input) = input.file
  override fun ExecContext.exec(input: Input): Output {
    val spoofax = Spx.spoofax()
    val langImpl = require(legacyBuildOrLoadLanguage.createTask(input.config)).v

    // Require parse table
    val facet = langImpl.facet(SyntaxFacet::class.java)
    if(facet != null) {
      val parseTableFile = facet.parseTable
      if(parseTableFile != null) {
        require(parseTableFile.pPath, FileStampers.hash)
      }
    }

    // Perform parsing
    val resource = input.file.fileObject
    val inputUnit = spoofax.unitService.inputUnit(resource, input.text, langImpl, null)
    return try {
      val parseUnit = spoofax.syntaxService.parse(inputUnit)
      val ast = parseUnit.ast()
      val tokens = if(ast != null) TokenExtractor.extract(ast) else null
      val messages = messageConverter.toMsgs(parseUnit.messages())
      Output(ast, tokens, messages, input.file)
    } catch(e: ParseException) {
      log.error("Parsing failed unexpectedly", e)
      Output(null, null, ArrayList(), input.file)
    }
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun createTask(config: SpxCoreConfig, text: String, file: PPath) = this.createTask(LegacyParse.Input(config, text, file))
}

class LegacyParseAll @Inject constructor(
  log: Logger,
  private val messageConverter: MessageConverter,
  private val legacyBuildOrLoadLanguage: LegacyBuildOrLoadLanguage
) : TaskDef<LegacyParseAll.Input, ArrayList<LegacyParseAll.Output>> {
  val log: Logger = log.forContext(LegacyParseAll::class.java)

  companion object {
    const val id = "legacy.ParseAll"
  }

  data class TextFilePair(val text: String, val file: PPath) : Tuple2<String, PPath>
  data class Input(val config: SpxCoreConfig, val pairs: ArrayList<TextFilePair>) : Serializable
  data class Output(val ast: IStrategoTerm?, val tokens: ArrayList<Token>?, val messages: ArrayList<Msg>, val file: PPath) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  override val id = Companion.id
  override fun key(input: Input) = input.pairs.map { it.file }.toCollection(ArrayList())
  override fun ExecContext.exec(input: Input): ArrayList<Output> {
    val spoofax = Spx.spoofax()
    val langImpl = require(legacyBuildOrLoadLanguage.createTask(input.config)).v

    // Require parse table
    val facet = langImpl.facet(SyntaxFacet::class.java)
    if(facet != null) {
      val parseTableFile = facet.parseTable
      if(parseTableFile != null) {
        require(parseTableFile.pPath, FileStampers.hash)
      }
    }

    // Perform parsing
    val inputs = input.pairs.map { (text, file) -> spoofax.unitService.inputUnit(file.fileObject, text, langImpl, null) }
    return try {
      val parseUnits = spoofax.syntaxService.parseAll(inputs)
      parseUnits.map {
        val ast = it.ast()
        val tokens = if(ast != null) TokenExtractor.extract(ast) else null
        val messages = messageConverter.toMsgs(it.messages())
        Output(ast, tokens, messages, it.source()?.pPath!!)
      }.toCollection(ArrayList())
    } catch(e: ParseException) {
      log.error("Parsing failed unexpectedly", e)
      input.pairs.map { (_, file) -> Output(null, null, ArrayList(), file) }.toCollection(ArrayList())
    }
  }
}
