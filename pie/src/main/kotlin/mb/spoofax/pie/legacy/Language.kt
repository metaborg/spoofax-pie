package mb.spoofax.pie.legacy

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.api.stamp.path.RecModifiedFileStamper
import mb.pie.vfs.path.PPath
import mb.pie.vfs.path.PathSrv
import mb.spoofax.runtime.cfg.SpxCoreConfig
import org.metaborg.core.build.CommonPaths
import org.metaborg.core.language.*
import org.metaborg.spoofax.core.Spoofax
import java.io.*
import java.util.zip.ZipInputStream

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
  inline fun createTask(dir: PPath, isLangSpec: Boolean) = this.createTask(Input(dir, isLangSpec))
}

class LegacyUnpackMetaLanguages @Inject constructor(
  private val pathSrv: PathSrv
) : TaskDef<PPath, LegacyUnpackMetaLanguages.Output> {
  companion object {
    const val id = "legacy.UnpackMetaLanguages"
  }

  data class Output(
    val spoofaxLib: PPath,
    val esv: PPath,
    val stratego: PPath,
    val sdf3: PPath,
    val nabl2Shared: PPath,
    val nabl2Runtime: PPath,
    val nabl2Lang: PPath
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): Output {
    // Install coarse-grained meta-dependency.
    val codeSource = javaClass.protectionDomain.codeSource
    if(codeSource != null) {
      val path = pathSrv.resolve(codeSource.location.toURI())
      require(path)
    } else {
      throw ExecException("Cannot install meta-dependency to the JAR/class file of LegacyUnpackMetaLanguages")
    }
    // Clean up unpack dir.
    val unpackDir = input.resolve(".spoofax_languages")
    unpackDir.deleteAll()
    unpackDir.createDirectories()
    // Unpack meta-languages and libraries.
    val spoofaxLib = unpackResourceTo("spoofax.lib.spoofax-language", unpackDir)
    val esv = unpackResourceTo("esv.spoofax-language", unpackDir)
    val stratego = unpackResourceTo("stratego.spoofax-language", unpackDir)
    val sdf3 = unpackResourceTo("sdf3.spoofax-language", unpackDir)
    val nabl2Shared = unpackResourceTo("nabl2.shared.spoofax-language", unpackDir)
    val nabl2Runtime = unpackResourceTo("nabl2.runtime.spoofax-language", unpackDir)
    val nabl2Lang = unpackResourceTo("nabl2.lang.spoofax-language", unpackDir)
    return Output(spoofaxLib, esv, stratego, sdf3, nabl2Shared, nabl2Runtime, nabl2Lang)
  }

  private fun ExecContext.unpackResourceTo(resource: String, directory: PPath): PPath {
    val finalDirectory = directory.resolve(resource)
    finalDirectory.createDirectories()
    javaClass.classLoader.getResourceAsStream("spoofax_languages/$resource").use { unpackTo(it, finalDirectory) }
    // TODO: is making a requires dependency to all recursive files too slow here?
    require(finalDirectory, RecModifiedFileStamper())
    return finalDirectory
  }

  private fun unpackTo(inputStream: InputStream, directory: PPath) {
    ZipInputStream(inputStream).use { zip ->
      while(true) {
        val entry = zip.nextEntry ?: break
        val path = directory.resolve(entry.name)
        // TODO: validate path?
        if(entry.isDirectory) {
          path.createDirectories();
        } else {
          path.outputStream().use { outputStream ->
            val bufferSize = 8192
            BufferedOutputStream(outputStream, bufferSize).use { bufferedOutputStream ->
              val buffer = ByteArray(bufferSize)
              while(true) {
                val readBytes = zip.read(buffer, 0, bufferSize)
                if(readBytes == -1) {
                  break
                }
                bufferedOutputStream.write(buffer, 0, readBytes)
              }
              // TODO: flush bufferedOutputStream?
            }
            // TODO: flush outputStream?
          }
        }
        zip.closeEntry()
      }
    }
  }
}

class LegacyLoadMetaLanguages @Inject constructor(
  private val legacyUnpackMetaLanguages: LegacyUnpackMetaLanguages
) : TaskDef<PPath, OutTransient<LegacyLoadMetaLanguages.Output>> {
  companion object {
    const val id = "legacy.LoadMetaLanguages"
  }

  data class Output(
    val spoofaxLib: ILanguageComponent,
    val esv: ILanguageComponent,
    val stratego: ILanguageComponent,
    val sdf3: ILanguageComponent,
    val nabl2Shared: ILanguageComponent,
    val nabl2Runtime: ILanguageComponent,
    val nabl2Lang: ILanguageComponent
  )

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): OutTransient<Output> {
    val unpacked = require(legacyUnpackMetaLanguages, input)
    val spoofax = Spx.spoofax()
    val spoofaxLib = spoofax.loadLang(unpacked.spoofaxLib)
    val esv = spoofax.loadLang(unpacked.esv)
    val stratego = spoofax.loadLang(unpacked.stratego)
    val sdf3 = spoofax.loadLang(unpacked.sdf3)
    val nabl2Shared = spoofax.loadLang(unpacked.nabl2Shared)
    val nabl2Runtime = spoofax.loadLang(unpacked.nabl2Runtime)
    val nabl2Lang = spoofax.loadLang(unpacked.nabl2Lang)
    return OutTransientImpl(Output(spoofaxLib, esv, stratego, sdf3, nabl2Shared, nabl2Runtime, nabl2Lang))
  }

  private fun Spoofax.loadLang(directory: PPath): ILanguageComponent {
    val request = languageComponentFactory.requestFromDirectory(directory.fileObject)
    val config = languageComponentFactory.createConfig(request)
    return languageService.add(config)
  }
}