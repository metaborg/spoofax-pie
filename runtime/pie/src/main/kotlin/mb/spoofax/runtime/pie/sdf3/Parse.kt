package mb.spoofax.runtime.pie.sdf3

import mb.pie.api.*
import mb.pie.builtin.util.Tuple3
import mb.spoofax.runtime.impl.sdf.Table
import mb.spoofax.runtime.model.message.Msg
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.pie.generated.createWorkspaceConfig
import mb.vfs.path.PPath
import org.spoofax.interpreter.terms.IStrategoTerm
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory
import org.spoofax.terms.TermFactory
import java.io.Serializable
import java.util.*

class Parse : TaskDef<Parse.Input, Parse.Output> {
  companion object {
    const val id = "sdf3.Parse"
  }

  data class Input(
    val text: String,
    val table: Table,
    val file: PPath,
    val langSpecExt: String,
    val root: PPath
  ) : Serializable

  data class Output(
    val ast: IStrategoTerm?,
    val tokenStream: ArrayList<Token>?,
    val messages: ArrayList<Msg>
  ) : Tuple3<IStrategoTerm?, ArrayList<Token>?, ArrayList<Msg>>

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Output {
    val (text, table, file, langSpecExt, root) = input
    val workspace =
      requireOutput(createWorkspaceConfig::class.java, createWorkspaceConfig.Companion.id, root)
        ?: throw ExecException("Could not create workspace config for root $root")
    val langSpec =
      workspace.langSpecConfigForExt(input.langSpecExt)
        ?: throw ExecException("Could not get language specification config for extension $langSpecExt")
    val startSymbol = langSpec.syntaxParseStartSymbolId()

    val termFactory = ImploderOriginTermFactory(TermFactory())
    val parser = table.createParser(termFactory)
    val output = parser.parse(text, startSymbol, file)
    return Output(output.ast, output.tokenStream, output.messages)
  }
}
