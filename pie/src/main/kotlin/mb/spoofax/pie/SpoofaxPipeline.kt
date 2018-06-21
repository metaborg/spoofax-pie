package mb.spoofax.pie

import com.google.inject.Inject
import mb.pie.api.Task
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.generated.*
import mb.spoofax.pie.processing.*

class SpoofaxPipeline @Inject constructor(
  private val _processWorkspace: processWorkspace,
  private val _processProject: processProject,
  private val _processEditor: processEditor
) {
  fun workspace(root: PPath): Task<PPath, WorkspaceResult> {
    return Task(_processWorkspace, root)
  }

  fun project(project: PPath, root: PPath): Task<processProject.Input, ProjectResult> {
    return Task(_processProject, processProject.Input(project, root))
  }

  fun editor(document: PPath, project: PPath, root: PPath, text: String): Task<processEditor.Input, DocumentResult> {
    return Task(_processEditor, processEditor.Input(document, project, root, text))
  }
}
