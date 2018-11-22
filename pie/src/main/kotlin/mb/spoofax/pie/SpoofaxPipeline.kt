package mb.spoofax.pie

import com.google.inject.Inject
import mb.fs.java.JavaFSPath
import mb.pie.api.Task
import mb.spoofax.pie.generated.*
import mb.spoofax.pie.processing.*

class SpoofaxPipeline @Inject constructor(
  private val _processWorkspace: processWorkspace,
  private val _processContainer: processContainer,
  private val _processDocumentWithText: processDocumentWithText,
  private val _processDocument: processDocument
) {
  fun workspace(root: JavaFSPath): Task<JavaFSPath, WorkspaceResult> {
    return Task(_processWorkspace, root)
  }

  fun container(container: JavaFSPath, root: JavaFSPath): Task<processContainer.Input, ContainerResult> {
    return Task(_processContainer, processContainer.Input(container, root))
  }

  fun documentWithText(document: JavaFSPath, container: JavaFSPath, root: JavaFSPath, text: String): Task<processDocumentWithText.Input, DocumentResult> {
    return Task(_processDocumentWithText, processDocumentWithText.Input(document, container, root, text))
  }

  fun document(document: JavaFSPath, container: JavaFSPath, root: JavaFSPath): Task<processDocument.Input, DocumentResult> {
    return Task(_processDocument, processDocument.Input(document, container, root))
  }
}
