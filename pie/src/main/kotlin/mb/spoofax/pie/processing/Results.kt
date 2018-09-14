package mb.spoofax.pie.processing

import mb.pie.vfs.path.PPath
import mb.spoofax.api.message.Message
import mb.spoofax.api.parse.Token
import mb.spoofax.api.style.Styling
import mb.spoofax.runtime.analysis.Analyzer
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

data class WorkspaceResult(
  val root: PPath,
  val containerResults: ArrayList<ContainerResult>
) : Serializable

fun createWorkspaceResult(root: PPath, containerResults: ArrayList<ContainerResult>) = WorkspaceResult(root, containerResults)

fun emptyWorkspaceResult(root: PPath) = WorkspaceResult(root, arrayListOf())


data class ContainerResult(
  val container: PPath,
  val documentResults: ArrayList<DocumentResult>
) : Serializable

fun createContainerResult(container: PPath, langSpecResults: ArrayList<DocumentResult>, legacyResults: ArrayList<DocumentResult>) = ContainerResult(container, (langSpecResults + legacyResults).toCollection(ArrayList()))

fun emptyContainerResult(container: PPath) = ContainerResult(container, arrayListOf())


data class DocumentResult(
  val document: PPath,
  val messages: ArrayList<Message>,
  val tokens: ArrayList<Token>?,
  val ast: IStrategoTerm?,
  val styling: Styling?,
  val analysis: Analyzer.FinalOutput?
) : Serializable

fun createDocumentResult(
  document: PPath,
  messages: ArrayList<Message>,
  tokens: ArrayList<Token>?,
  ast: IStrategoTerm?,
  styling: Styling?,
  analysis: Analyzer.FinalOutput?
) = DocumentResult(document, messages, tokens, ast, styling, analysis)

fun emptyDocumentResult(document: PPath) = DocumentResult(document, arrayListOf(), null, null, null, null)
