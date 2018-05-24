package mb.spoofax.pie.esv

import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.spoofax.api.parse.Token
import mb.spoofax.api.style.Styling
import mb.spoofax.runtime.esv.Styler
import mb.spoofax.runtime.esv.StylingRules
import java.io.Serializable

class Style : TaskDef<Style.Input, Styling> {
  companion object {
    const val id = "Style"
  }

  data class Input(
    val tokenStream: ArrayList<Token>,
    val rules: StylingRules
  ) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Styling {
    val styler = Styler(input.rules)
    return styler.style(input.tokenStream)
  }
}
