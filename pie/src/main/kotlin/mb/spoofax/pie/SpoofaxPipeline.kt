package mb.spoofax.pie

import com.google.inject.Inject
import mb.pie.api.Task
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.generated.*
import mb.spoofax.pie.processing.*

class SpoofaxPipeline @Inject constructor(
  private val _processWorkspace: processWorkspace,
  private val _processContainer: processContainer,
  private val _processDocumentWithText: processDocumentWithText,
  private val _processDocument: processDocument
) {
  fun workspace(root: PPath): Task<PPath, WorkspaceResult> {
    return Task(_processWorkspace, root)
  }

  fun container(project: PPath, root: PPath): Task<processContainer.Input, ContainerResult> {
    return Task(_processContainer, processContainer.Input(project, root))
  }

  fun documentWithText(document: PPath, project: PPath, root: PPath, text: String): Task<processDocumentWithText.Input, DocumentResult> {
    return Task(_processDocumentWithText, processDocumentWithText.Input(document, project, root, text))
  }

  fun document(document: PPath, project: PPath, root: PPath): Task<processDocument.Input, DocumentResult> {
    return Task(_processDocument, processDocument.Input(document, project, root))
  }
}
