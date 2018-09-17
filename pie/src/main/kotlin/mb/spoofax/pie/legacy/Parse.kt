package mb.spoofax.pie.legacy

import com.google.inject.Inject
import mb.log.api.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.lang.runtime.util.Tuple3
import mb.pie.vfs.path.PPath
import mb.spoofax.api.message.Message
import mb.spoofax.api.parse.Token
import mb.spoofax.legacy.MessageConverter
import mb.spoofax.runtime.jsglr.TokenExtractor
import org.metaborg.core.syntax.ParseException
import org.metaborg.spoofax.core.syntax.SyntaxFacet
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

class LegacyParse @Inject constructor(
  logFactory: Logger,
  private val messageConverter: MessageConverter
) : TaskDef<LegacyParse.Input, LegacyParse.Output> {
  private val log: Logger = logFactory.forContext(LegacyParse::class.java)

  companion object {
    const val id = "legacy.Parse"
  }

  data class Input(val file: PPath, val text: String) : Serializable
  data class Output(val ast: IStrategoTerm?, val tokens: ArrayList<Token>?, val messages: ArrayList<Message>) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Message>>

  override val id = Companion.id
  override fun key(input: Input) = input.file
  override fun ExecContext.exec(input: Input): Output {
    val (file, text) = input
    val resource = file.fileObject
    val spoofax = Spx.spoofax()
    val langImpl = spoofax.languageIdentifierService.identify(resource)
      ?: throw ExecException("Cannot parse; could not identify language of file $file")

    // Require parse table
    val facet = langImpl.facet(SyntaxFacet::class.java)
    if(facet != null) {
      val parseTableFile = facet.parseTable
      if(parseTableFile != null) {
        require(parseTableFile.pPath, FileStampers.hash)
      }
    }

    // Perform parsing
    val inputUnit = spoofax.unitService.inputUnit(resource, text, langImpl, null)
    return try {
      val parseUnit = spoofax.syntaxService.parse(inputUnit)
      val ast = parseUnit.ast()
      val tokens = if(ast != null) TokenExtractor.extract(ast) else null
      val messages = messageConverter.toMessages(parseUnit.messages())
      Output(ast, tokens, messages)
    } catch(e: ParseException) {
      log.error("Parsing failed unexpectedly", e)
      Output(null, null, ArrayList())
    }
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun createTask(file: PPath, text: String) = this.createTask(LegacyParse.Input(file, text))
}

class LegacyParseAll @Inject constructor(
  logFactory: Logger,
  private val messageConverter: MessageConverter
) : TaskDef<ArrayList<FileTextPair>, ArrayList<LegacyParseAll.Output>> {
  val log: Logger = logFactory.forContext(LegacyParseAll::class.java)

  companion object {
    const val id = "legacy.ParseAll"
  }

  data class Output(val ast: IStrategoTerm?, val tokens: ArrayList<Token>?, val messages: ArrayList<Message>, val file: PPath) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Message>>

  override val id = Companion.id
  override fun key(input: ArrayList<FileTextPair>) = input.map { it.file }.toCollection(ArrayList())
  override fun ExecContext.exec(input: ArrayList<FileTextPair>): ArrayList<Output> {
    val pairs = input
    if(pairs.isEmpty()) {
      return arrayListOf()
    }
    val spoofax = Spx.spoofax()
    val firstFile = pairs.first().file
    val langImpl = spoofax.languageIdentifierService.identify(firstFile.fileObject)
      ?: throw ExecException("Could not identify language of file $firstFile")

    // Require parse table
    val facet = langImpl.facet(SyntaxFacet::class.java)
    if(facet != null) {
      val parseTableFile = facet.parseTable
      if(parseTableFile != null) {
        require(parseTableFile.pPath, FileStampers.hash)
      }
    }

    // Perform parsing
    val inputs = pairs.map { (file, text) -> spoofax.unitService.inputUnit(file.fileObject, text, langImpl, null) }
    return try {
      val parseUnits = spoofax.syntaxService.parseAll(inputs)
      parseUnits.map {
        val ast = it.ast()
        val tokens = if(ast != null) TokenExtractor.extract(ast) else null
        val messages = messageConverter.toMessages(it.messages())
        Output(ast, tokens, messages, it.source()?.pPath!!)
      }.toCollection(ArrayList())
    } catch(e: ParseException) {
      log.error("Parsing failed unexpectedly", e)
      pairs.map { (file, _) -> Output(null, null, ArrayList(), file) }.toCollection(ArrayList())
    }
  }
}
