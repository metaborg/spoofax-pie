package mb.spoofax.pie

import com.google.inject.Inject
import mb.pie.api.Task
import mb.pie.lang.runtime.util.*
import mb.pie.vfs.path.PPath
import mb.spoofax.api.message.Msg
import mb.spoofax.api.parse.Token
import mb.spoofax.api.style.Styling
import mb.spoofax.pie.generated.*
import mb.spoofax.runtime.nabl.ConstraintSolverSolution

class SpoofaxPipeline @Inject constructor(
  private val processWorkspace: processWorkspace,
  private val _processProject: processProject,
  private val _processEditor: processEditor
) {
  fun workspace(root: PPath): Task<PPath, ArrayList<Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?, ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?>>>>> {
    return Task(processWorkspace, root)
  }

  fun project(project: PPath, root: PPath): Task<processProject.Input, processProject.Output?> {
    return Task(_processProject, processProject.Input(project, root))
  }

  fun editor(text: String, file: PPath, project: PPath, root: PPath): Task<processEditor.Input, processEditor.Output?> {
    return Task(_processEditor, processEditor.Input(text, file, project, root))
  }
}
