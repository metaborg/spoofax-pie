package mb.pipe.run.ceres.spoofax.core

import com.google.inject.Inject
import mb.ceres.BuildContext
import mb.ceres.BuildException
import mb.ceres.Builder
import mb.ceres.PathStampers
import mb.pipe.run.ceres.path.cPath
import mb.pipe.run.core.log.Logger
import mb.pipe.run.spoofax.esv.ESVReader
import mb.pipe.run.spoofax.util.EsvUtil
import org.metaborg.core.language.LanguageIdentifier

import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths

class CoreExtensions @Inject constructor(log: Logger) : Builder<LanguageIdentifier, ArrayList<String>> {
  companion object {
    val id = "coreExtensions"
  }


  val log: Logger = log.forContext(CoreTrans::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: LanguageIdentifier): ArrayList<String> {
    val spoofax = Spx.spoofax()
    val langImpl = spoofax.languageService.getImpl(input) ?: throw BuildException("Language with id $input does not exist or has not been loaded into Spoofax core")

    // Require packed ESV file
    val langLoc = langImpl.components().first().location()
    val packedEsvFile = SpoofaxLangSpecCommonPaths(langLoc).targetMetaborgDir().resolveFile("editor.esv.af").pPath
    require(packedEsvFile.cPath, PathStampers.hash)

    // Get extensions
    val esvTerm = EsvUtil.read(packedEsvFile)
    val extensionsStr = ESVReader.getProperty(esvTerm, "Extensions") ?: return ArrayList()
    return ArrayList(extensionsStr.split(","))
  }
}