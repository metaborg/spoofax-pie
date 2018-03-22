package mb.spoofax.runtime.pie.builder

import mb.pie.runtime.builtin.util.*
import mb.pie.runtime.core.FuncApp
import mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution
import mb.spoofax.runtime.model.message.Msg
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.model.style.Styling
import mb.spoofax.runtime.pie.generated.*
import mb.vfs.path.PPath
import org.metaborg.meta.nabl2.solver.ImmutablePartialSolution


object SpoofaxPipeline {
  fun workspace(root: PPath): FuncApp<PPath, ArrayList<Tuple2<ArrayList<Tuple2<ArrayList<Tuple5<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?, ImmutablePartialSolution?>>, ArrayList<ConstraintSolverSolution?>>>, ArrayList<Tuple4<PPath, ArrayList<Token>?, ArrayList<Msg>, Styling?>>>?>> {
    return FuncApp(processWorkspace::class.java, processWorkspace.id, root)
  }

  fun project(project: PPath, root: PPath): FuncApp<processProject.Input, processProject.Output?> {
    return FuncApp(processProject::class.java, processProject.id, processProject.Input(project, root))
  }

  fun editor(text: String, file: PPath, project: PPath, root: PPath): FuncApp<processEditor.Input, processEditor.Output?> {
    return FuncApp(processEditor::class.java, processEditor.id, processEditor.Input(text, file, project, root))
  }
}
