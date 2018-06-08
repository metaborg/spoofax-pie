package mb.spoofax.pie.legacy

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.api.stamp.FileStampers
import mb.spoofax.api.parse.Token
import mb.spoofax.api.style.Styling
import mb.spoofax.legacy.StyleConverter
import mb.spoofax.runtime.cfg.SpxCoreConfig
import org.metaborg.core.messages.IMessage
import org.metaborg.spoofax.core.unit.ParseContrib
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths
import org.metaborg.util.iterators.Iterables2
import org.spoofax.interpreter.terms.IStrategoTerm
import java.io.Serializable

class LegacyStyle @Inject constructor(
  log: Logger,
  private val legacyBuildOrLoadLanguage: LegacyBuildOrLoadLanguage
) : TaskDef<LegacyStyle.Input, Styling> {
  val log: Logger = log.forContext(LegacyStyle::class.java)

  companion object {
    const val id = "legacy.Style"
  }

  data class Input(val config: SpxCoreConfig, val tokenStream: Iterable<Token>, val ast: IStrategoTerm) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Styling {
    val spoofax = Spx.spoofax()
    val langImpl = require(legacyBuildOrLoadLanguage.createTask(input.config)).v

    // Require packed ESV file
    val langLoc = langImpl.components().first().location()
    val packedEsvFile = SpoofaxLangSpecCommonPaths(langLoc).targetMetaborgDir().resolveFile("editor.esv.af")
    require(packedEsvFile.pPath, FileStampers.hash)

    // Perform styling
    val inputUnit = spoofax.unitService.inputUnit("hack", langImpl, null)
    val parseUnit = spoofax.unitService.parseUnit(inputUnit, ParseContrib(true, true, input.ast, Iterables2.empty<IMessage>(), -1))
    val categorization = spoofax.categorizerService.categorize(langImpl, parseUnit)
    return StyleConverter.toStyling(spoofax.stylerService.styleParsed(langImpl, categorization))
  }
}