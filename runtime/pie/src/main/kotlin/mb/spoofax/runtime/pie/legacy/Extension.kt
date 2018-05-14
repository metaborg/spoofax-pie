package mb.spoofax.runtime.pie.legacy

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.core.*
import mb.pie.runtime.core.stamp.PathStampers
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.spoofax.runtime.impl.esv.ESVReader
import mb.spoofax.runtime.impl.legacy.EsvUtil
import mb.vfs.path.PPath
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths
import java.io.Serializable

class CoreExtensions @Inject constructor(log: Logger) : Func<CoreExtensions.Input, ArrayList<String>> {
  companion object {
    val id = "coreExtensions"
  }

  data class Input(val dir: PPath, val isLangSpec: Boolean) : Serializable {
    constructor(config: SpxCoreConfig) : this(config.dir(), config.isLangSpec)
  }

  val log: Logger = log.forContext(CoreExtensions::class.java)

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): ArrayList<String> {
    val langImpl = buildOrLoad(input.dir, input.isLangSpec)

    // Require packed ESV file
    val langLoc = langImpl.components().first().location()
    val packedEsvFile = SpoofaxLangSpecCommonPaths(langLoc).targetMetaborgDir().resolveFile("editor.esv.af").pPath
    if(packedEsvFile.exists()) {
      require(packedEsvFile, PathStampers.hash)

      // Get extensions
      val esvTerm = EsvUtil.read(packedEsvFile)
      val extensionsStr = ESVReader.getProperty(esvTerm, "Extensions") ?: return ArrayList()
      return ArrayList(extensionsStr.split(","))
    } else {
      require(packedEsvFile, PathStampers.exists)
      return ArrayList()
    }
  }
}

fun ExecContext.langExtensions(input: CoreExtensions.Input) = requireOutput(CoreExtensions::class, CoreExtensions.id, input)
fun ExecContext.langExtensions(dir: PPath, isLangSpec: Boolean) = langExtensions(CoreExtensions.Input(dir, isLangSpec))
fun ExecContext.langExtensions(input: SpxCoreConfig) = langExtensions(CoreExtensions.Input(input))
