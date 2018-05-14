package mb.spoofax.runtime.pie.legacy

import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.FileStampers
import mb.vfs.path.PPath
import org.metaborg.core.build.CommonPaths
import org.metaborg.core.language.*

typealias TransientLangImpl = OutTransientEquatable<ILanguageImpl, LanguageIdentifier>

class CoreLoadLang : TaskDef<PPath, TransientLangImpl> {
  companion object {
    const val id = "coreLoadLang"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): TransientLangImpl {
    val spoofax = Spx.spoofax()
    val resource = input.fileObject
    val request: IComponentCreationConfigRequest
    if(resource.isFile) {
      request = spoofax.languageComponentFactory.requestFromArchive(resource)
      require(input, FileStampers.hash)
    } else {
      request = spoofax.languageComponentFactory.requestFromDirectory(resource)
      val paths = CommonPaths(resource)
      require(paths.targetMetaborgDir().pPath, FileStampers.hash)
    }
    val config = spoofax.languageComponentFactory.createConfig(request)
    val component = spoofax.languageService.add(config)
    val impl = component.contributesTo().first()
    return OutTransientEquatableImpl(impl, impl.id())
  }
}

fun ExecContext.loadLangRaw(input: PPath) = requireOutput(CoreLoadLang::class.java, CoreLoadLang.id, input)
fun ExecContext.loadLang(input: PPath) = loadLangRaw(input).v