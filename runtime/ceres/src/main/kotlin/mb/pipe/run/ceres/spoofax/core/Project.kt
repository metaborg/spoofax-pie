package mb.pipe.run.ceres.spoofax.core

import mb.ceres.BuildContext
import mb.ceres.Builder
import mb.ceres.OutTransient
import mb.pipe.run.core.path.PPath
import org.metaborg.core.project.IProject
import org.metaborg.core.project.ISimpleProjectService

class CoreLoadProj : Builder<PPath, OutTransient<CoreLoadProj.Project>> {
  companion object {
    val id = "coreLoadProj"
  }

  class Project(private val spxCoreProject: IProject) {
    fun directory(): PPath {
      return spxCoreProject.location().pPath;
    }
  }

  override val id = Companion.id
  override fun BuildContext.build(input: PPath): OutTransient<Project> {
    val spoofax = Spx.spoofax()
    val projLoc = input.fileObject
    var project = spoofax.projectService.get(projLoc)
    if (project == null) {
      project = spoofax.injector.getInstance(ISimpleProjectService::class.java).create(projLoc)
    }
    return OutTransient(Project(project!!))
  }
}

fun BuildContext.loadProj(input: PPath) = requireOutput(CoreLoadProj::class.java, input).v