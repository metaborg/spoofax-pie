package mb.spoofax.pie.processing

import mb.fs.java.JavaFSPath
import mb.spoofax.api.message.Message
import mb.spoofax.api.parse.Token
import mb.spoofax.api.style.Styling
import mb.spoofax.runtime.analysis.Analyzer
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

data class WorkspaceResult(
  val root: JavaFSPath,
  val containerResults: ArrayList<ContainerResult>
) : Serializable

fun createWorkspaceResult(root: JavaFSPath, containerResults: ArrayList<ContainerResult>) = WorkspaceResult(root, containerResults)

fun emptyWorkspaceResult(root: JavaFSPath) = WorkspaceResult(root, arrayListOf())


data class ContainerResult(
  val container: JavaFSPath,
  val documentResults: ArrayList<DocumentResult>
) : Serializable

fun createContainerResult(container: JavaFSPath, langSpecResults: ArrayList<DocumentResult>, legacyResults: ArrayList<DocumentResult>) = ContainerResult(container, (langSpecResults + legacyResults).toCollection(ArrayList()))

fun emptyContainerResult(container: JavaFSPath) = ContainerResult(container, arrayListOf())


data class DocumentResult(
  val document: JavaFSPath,
  val messages: ArrayList<Message>,
  val tokens: ArrayList<Token>?,
  val ast: IStrategoTerm?,
  val styling: Styling?,
  val analysis: Analyzer.FinalOutput?
) : Serializable

fun createDocumentResult(
  document: JavaFSPath,
  messages: ArrayList<Message>,
  tokens: ArrayList<Token>?,
  ast: IStrategoTerm?,
  styling: Styling?,
  analysis: Analyzer.FinalOutput?
) = DocumentResult(document, messages, tokens, ast, styling, analysis)

fun emptyDocumentResult(document: JavaFSPath) = DocumentResult(document, arrayListOf(), null, null, null, null)
