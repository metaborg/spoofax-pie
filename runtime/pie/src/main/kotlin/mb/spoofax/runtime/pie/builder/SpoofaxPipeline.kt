package mb.spoofax.runtime.pie.builder

import mb.pie.runtime.core.*
import mb.pie.runtime.core.exec.AnyObsFuncApp
import mb.pie.runtime.core.exec.ObsFuncApp
import mb.spoofax.runtime.pie.generated.processEditor
import mb.spoofax.runtime.pie.generated.processProject
import mb.vfs.path.PPath


object SpoofaxPipeline {
  fun project(project: PPath, root: PPath): FuncApp<processProject.Input, processProject.Output?> {
    return FuncApp(processProject::class.java, processProject.id, processProject.Input(project, root))
  }

  fun editor(text: String, file: PPath, project: PPath, root: PPath): FuncApp<processEditor.Input, processEditor.Output?> {
    return FuncApp(processEditor::class.java, processEditor.id, processEditor.Input(text, file, project, root))
  }
}
