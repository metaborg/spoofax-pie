package mb.spoofax.pie.jsglr

import com.google.inject.Inject
import mb.pie.api.*
import mb.pie.lang.runtime.util.Tuple3
import mb.pie.vfs.path.PPath
import mb.spoofax.api.message.Message
import mb.spoofax.api.parse.Token
import mb.spoofax.pie.config.ParseWorkspaceConfig
import mb.spoofax.pie.config.requireConfigValue
import mb.spoofax.runtime.cfg.LangId
import mb.spoofax.runtime.jsglr.Table
import org.spoofax.interpreter.terms.IStrategoTerm
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory
import org.spoofax.terms.TermFactory
import java.io.Serializable
import java.util.*

class JSGLRParse @Inject constructor(
  private val parseWorkspaceConfig: ParseWorkspaceConfig
) : TaskDef<JSGLRParse.Input, JSGLRParse.Output> {
  companion object {
    const val id = "jsglr.Parse"
  }

  data class Input(val document: PPath, val langId: LangId, val root: PPath, val text: String, val table: Table) : Serializable
  data class Key(val document: PPath, val langId: LangId, val root: PPath) : Serializable {
    constructor(input: Input) : this(input.document, input.langId, input.root)
  }

  data class Output(
    val ast: IStrategoTerm?,
    val tokenStream: ArrayList<Token>?,
    val messages: ArrayList<Message>
  ) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Message>>

  override val id = Companion.id
  override fun key(input: Input) = Key(input)
  override fun ExecContext.exec(input: Input): Output {
    val (document, langId, root, text, table) = input
    val startSymbol = requireConfigValue(this, parseWorkspaceConfig, root) { workspaceConfig ->
      workspaceConfig.langSpecConfigForId(langId)?.syntaxParseStartSymbolId()
    } ?: throw ExecException("Could not get language specification configuration for language with identifier $langId")
    val termFactory = ImploderOriginTermFactory(TermFactory())
    val parser = table.createParser(termFactory)
    val output = parser.parse(text, startSymbol, document)
    return Output(output.ast, output.tokenStream, output.messages)
  }
}
