package mb.spoofax.pie.processing

import mb.pie.vfs.path.PPath
import mb.spoofax.api.message.Msg
import mb.spoofax.api.parse.Token
import mb.spoofax.api.style.Styling
import mb.spoofax.runtime.constraint.CSolution
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

data class WorkspaceResult(
  val root: PPath,
  val projectResults: ArrayList<ProjectResult>
) : Serializable

fun createWorkspaceResult(root: PPath, projectResults: ArrayList<ProjectResult>) = WorkspaceResult(root, projectResults)

fun emptyWorkspaceResult(root: PPath) = WorkspaceResult(root, arrayListOf())


data class ProjectResult(
  val project: PPath,
  val documentResults: ArrayList<DocumentResult>
) : Serializable

fun createProjectResult(project: PPath, langSpecResults: ArrayList<DocumentResult>, legacyResults: ArrayList<DocumentResult>) = ProjectResult(project, (langSpecResults + legacyResults).toCollection(ArrayList()))

fun emptyProjectResult(project: PPath) = ProjectResult(project, arrayListOf())


data class DocumentResult(
  val document: PPath,
  val messages: ArrayList<Msg>,
  val tokens: ArrayList<Token>?,
  val ast: IStrategoTerm?,
  val styling: Styling?,
  val constraintsSolution: CSolution?
) : Serializable

fun createDocumentResult(
  document: PPath,
  messages: ArrayList<Msg>,
  tokens: ArrayList<Token>?,
  ast: IStrategoTerm?,
  styling: Styling?,
  constraintsSolution: CSolution?
) = DocumentResult(document, messages, tokens, ast, styling, constraintsSolution)

fun emptyDocumentResult(document: PPath) = DocumentResult(document, arrayListOf(), null, null, null, null)
