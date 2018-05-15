package mb.spoofax.runtime.pie

import mb.pie.builtin.util.*
import mb.pie.runtime.Task
import mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution
import mb.spoofax.runtime.model.message.Msg
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.model.style.Styling
import mb.spoofax.runtime.pie.generated.*
import mb.vfs.path.PPath


object SpoofaxPipeline {
  fun workspace(root: PPath): Task<PPath, ArrayList<Tuple2<ArrayList<ArrayList<Tuple5<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?, ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?>>>>> {
    return Task(processWorkspace::class.java, processWorkspace.id, root)
  }

  fun project(project: PPath, root: PPath): Task<processProject.Input, processProject.Output?> {
    return Task(processProject::class.java, processProject.id, processProject.Input(project, root))
  }

  fun editor(text: String, file: PPath, project: PPath, root: PPath): Task<processEditor.Input, processEditor.Output?> {
    return Task(processEditor::class.java, processEditor.id, processEditor.Input(text, file, project, root))
  }
}
