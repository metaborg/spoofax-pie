package mb.spoofax.pie.jsglr

import com.google.inject.Inject
import mb.pie.api.*
import mb.pie.lang.runtime.util.Tuple3
import mb.pie.vfs.path.PPath
import mb.spoofax.api.message.Msg
import mb.spoofax.api.parse.Token
import mb.spoofax.pie.generated.createWorkspaceConfig
import mb.spoofax.runtime.sdf.Table
import org.spoofax.interpreter.terms.IStrategoTerm
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory
import org.spoofax.terms.TermFactory
import java.io.Serializable
import java.util.*

class JSGLRParse @Inject constructor(
  private val createWorkspaceConfig: createWorkspaceConfig
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

    // OPTO: only depend on language specification config for langSpecExt.
    val workspaceConfig = require(createWorkspaceConfig, root)
      ?: throw ExecException("Could not get workspace config at root $root")

    // OPTO: only depend on syntax start symbol.
    val langSpec = workspaceConfig.langSpecConfigForExt(langSpecExt)
      ?: throw ExecException("Could not get language specification config for extension $langSpecExt")
    val startSymbol = langSpec.syntaxParseStartSymbolId()

    val termFactory = ImploderOriginTermFactory(TermFactory())
    val parser = table.createParser(termFactory)
    val output = parser.parse(text, startSymbol, file)
    return Output(output.ast, output.tokenStream, output.messages)
  }
}
