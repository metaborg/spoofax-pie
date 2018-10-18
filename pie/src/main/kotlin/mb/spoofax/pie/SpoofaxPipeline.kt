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

  fun container(container: PPath, root: PPath): Task<processContainer.Input, ContainerResult> {
    return Task(_processContainer, processContainer.Input(container, root))
  }

  fun documentWithText(document: PPath, container: PPath, root: PPath, text: String): Task<processDocumentWithText.Input, DocumentResult> {
    return Task(_processDocumentWithText, processDocumentWithText.Input(document, container, root, text))
  }

  fun document(document: PPath, container: PPath, root: PPath): Task<processDocument.Input, DocumentResult> {
    return Task(_processDocument, processDocument.Input(document, container, root))
  }
}
