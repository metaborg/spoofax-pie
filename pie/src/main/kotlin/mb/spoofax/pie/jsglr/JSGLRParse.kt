package mb.spoofax.pie.jsglr

import com.google.inject.Inject
import mb.pie.api.*
import mb.pie.lang.runtime.util.Tuple3
import mb.pie.vfs.path.PPath
import mb.spoofax.api.message.Msg
import mb.spoofax.api.parse.Token
import mb.spoofax.pie.config.ParseWorkspaceConfig
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

  data class Input(val text: String, val table: Table, val file: PPath, val langSpecExt: String, val root: PPath) : Serializable
  data class Key(val file: PPath, val langSpecExt: String, val root: PPath) : Serializable {
    constructor(input: Input) : this(input.file, input.langSpecExt, input.root)
  }

  data class Output(
    val ast: IStrategoTerm?,
    val tokenStream: ArrayList<Token>?,
    val messages: ArrayList<Msg>
  ) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  override val id = Companion.id
  override fun key(input: Input) = Key(input)
  override fun ExecContext.exec(input: Input): Output {
    val (text, table, file, langSpecExt, root) = input
    val startSymbol = with(parseWorkspaceConfig) {
      requireConfigValue(root) { workspaceConfig ->
        workspaceConfig.langSpecConfigForExt(langSpecExt)?.syntaxParseStartSymbolId()
      }
    } ?: throw ExecException("Could not get language specification configuration for language $langSpecExt")
    val termFactory = ImploderOriginTermFactory(TermFactory())
    val parser = table.createParser(termFactory)
    val output = parser.parse(text, startSymbol, file)
    return Output(output.ast, output.tokenStream, output.messages)
  }
}
