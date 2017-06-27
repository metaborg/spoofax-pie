package mb.pipe.run.ceres.spoofax.core

import mb.ceres.BuildContext
import mb.ceres.Builder
import mb.ceres.OutTransient
import mb.ceres.PathStampers
import mb.pipe.run.ceres.path.cPath
import mb.pipe.run.core.path.PPath
import org.metaborg.core.build.CommonPaths
import org.metaborg.core.language.IComponentCreationConfigRequest
import org.metaborg.core.language.ILanguageImpl

class CoreLoadLang : Builder<PPath, OutTransient<ILanguageImpl>> {
  companion object {
    val id = "coreLoadLang"
  }

  override val id = Companion.id
  override fun BuildContext.build(input: PPath): OutTransient<ILanguageImpl> {
    val spoofax = Spx.spoofax()
    val resource = input.fileObject
    val request: IComponentCreationConfigRequest
    if (resource.isFile) {
      request = spoofax.languageComponentFactory.requestFromArchive(resource)
      require(input.cPath, PathStampers.hash)
    } else {
      request = spoofax.languageComponentFactory.requestFromDirectory(resource)
      val paths = CommonPaths(resource)
      require(paths.targetMetaborgDir().cPath, PathStampers.hash)
    }
    val config = spoofax.languageComponentFactory.createConfig(request)
    val component = spoofax.languageService.add(config)
    val impl = component.contributesTo().first()
    return OutTransient(impl)
  }
}

fun BuildContext.loadLang(input: PPath) = requireOutput(CoreLoadLang::class.java, input).v