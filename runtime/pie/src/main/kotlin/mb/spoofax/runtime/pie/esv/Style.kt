package mb.spoofax.runtime.pie.esv

import mb.pie.runtime.core.ExecContext
import mb.pie.runtime.core.TaskDef
import mb.spoofax.runtime.impl.esv.Styler
import mb.spoofax.runtime.impl.esv.StylingRules
import mb.spoofax.runtime.model.parse.Token
import mb.spoofax.runtime.model.style.Styling
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
