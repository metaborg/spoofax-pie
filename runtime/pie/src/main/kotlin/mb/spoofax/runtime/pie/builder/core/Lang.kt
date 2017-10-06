package mb.spoofax.runtime.pie.builder.core

import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import org.metaborg.core.build.CommonPaths
import org.metaborg.core.language.*

typealias TransientLangImpl = OutTransientEquatable<ILanguageImpl, LanguageIdentifier>

class CoreLoadLang : Builder<PPath, TransientLangImpl> {
  companion object {
    val id = "coreLoadLang"
  }

  override val id = Companion.id
  override fun BuildContext.build(input: PPath): TransientLangImpl {
    val spoofax = Spx.spoofax()
    val resource = input.fileObject
    val request: IComponentCreationConfigRequest
    if(resource.isFile) {
      request = spoofax.languageComponentFactory.requestFromArchive(resource)
      require(input, PathStampers.hash)
    } else {
      request = spoofax.languageComponentFactory.requestFromDirectory(resource)
      val paths = CommonPaths(resource)
      require(paths.targetMetaborgDir().pPath, PathStampers.hash)
    }
    val config = spoofax.languageComponentFactory.createConfig(request)
    val component = spoofax.languageService.add(config)
    val impl = component.contributesTo().first()
    return OutTransientEquatableImpl(impl, impl.id())
  }
}

fun BuildContext.loadLangRaw(input: PPath) = requireOutput(CoreLoadLang::class.java, input)
fun BuildContext.loadLang(input: PPath) = loadLangRaw(input).v