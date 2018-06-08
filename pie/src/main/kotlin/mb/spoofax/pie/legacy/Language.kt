package mb.spoofax.pie.legacy

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.runtime.cfg.SpxCoreConfig
import org.metaborg.core.build.CommonPaths
import org.metaborg.core.language.*
import java.awt.SystemColor.text
import java.io.Serializable

typealias TransientLangImpl = OutTransientEquatable<ILanguageImpl, LanguageIdentifier>

class LegacyLoadLanguage : TaskDef<PPath, TransientLangImpl> {
  companion object {
    const val id = "legacy.LoadLanguage"
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

class LegacyBuildOrLoadLanguage @Inject constructor(
  log: Logger,
  private val legacyBuildProject: LegacyBuildProject,
  private val legacyBuildLangSpec: LegacyBuildLangSpec,
  private val legacyLoadLanguage: LegacyLoadLanguage
) : TaskDef<LegacyBuildOrLoadLanguage.Input, TransientLangImpl> {
  val log: Logger = log.forContext(LegacyBuildProject::class.java)

  companion object {
    const val id = "legacy.LegacyBuildOrLoad"
  }

  data class Input(val dir: PPath, val isLangSpec: Boolean) : Serializable {
    constructor(config: SpxCoreConfig) : this(config.dir(), config.isLangSpec)
  }

  override val id = Companion.id
  override fun key(input: Input) = input.dir
  override fun ExecContext.exec(input: Input): TransientLangImpl {
    val dir = input.dir
    if(input.isLangSpec) {
      require(legacyBuildProject, dir)
      require(legacyBuildLangSpec, dir)
    }
    return require(legacyLoadLanguage, dir)
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun createTask(config: SpxCoreConfig) = this.createTask(Input(config))

  @Suppress("NOTHING_TO_INLINE")
  inline fun createTask(dir: PPath, isLangSpec: Boolean) =  this.createTask(Input(dir, isLangSpec))
}
