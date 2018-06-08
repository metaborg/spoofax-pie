package mb.spoofax.pie.legacy

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.legacy.EsvUtil
import mb.spoofax.runtime.cfg.SpxCoreConfig
import mb.spoofax.runtime.esv.ESVReader
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths
import java.io.Serializable

class LegacyLanguageExtensions @Inject constructor(
  log: Logger,
  private val legacyBuildOrLoadLanguage: LegacyBuildOrLoadLanguage
) : TaskDef<LegacyLanguageExtensions.Input, ArrayList<String>> {
  val log: Logger = log.forContext(LegacyLanguageExtensions::class.java)

  companion object {
    const val id = "legacy.LanguageExtensions"
  }

  data class Input(val dir: PPath, val isLangSpec: Boolean) : Serializable {
    constructor(config: SpxCoreConfig) : this(config.dir(), config.isLangSpec)
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ArrayList<String> {
    val langImpl = require(legacyBuildOrLoadLanguage.createTask(input.dir, input.isLangSpec)).v

    // Require packed ESV file
    val langLoc = langImpl.components().first().location()
    val packedEsvFile = SpoofaxLangSpecCommonPaths(langLoc).targetMetaborgDir().resolveFile("editor.esv.af").pPath
    if(packedEsvFile.exists()) {
      require(packedEsvFile, FileStampers.hash)

      // Get extensions
      val esvTerm = EsvUtil.read(packedEsvFile)
      val extensionsStr = ESVReader.getProperty(esvTerm, "Extensions") ?: return ArrayList()
      return ArrayList(extensionsStr.split(","))
    } else {
      require(packedEsvFile, FileStampers.exists)
      return ArrayList()
    }
  }
}
