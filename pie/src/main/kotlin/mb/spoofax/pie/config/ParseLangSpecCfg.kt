package mb.spoofax.pie.config

import com.google.inject.Inject
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.legacy.LegacyParse
import mb.spoofax.runtime.cfg.LangSpecConfig
import mb.spoofax.runtime.cfg.SpxCoreConfig
import java.io.Serializable

class ParseLangSpecCfg @Inject constructor(
  private val legacyParse: LegacyParse
) : TaskDef<ParseLangSpecCfg.Input, LangSpecConfig?> {
  companion object {
    const val id = "config.ParseLangSpecCfg"
  }

  data class Input(val configLangCfg: SpxCoreConfig, val file: PPath) : Serializable

  override val id: String = Companion.id
  override fun key(input: Input) = input.file
  override fun ExecContext.exec(input: Input): LangSpecConfig? {
    val file = input.file
    require(file)
    val text = String(file.readAllBytes())
    val (ast, _, _) = require(legacyParse.createTask(input.configLangCfg, text, file))
    if(ast == null) {
      return null
    }
    val dir = file.parent()
    return LangSpecConfig.fromTerm(ast, dir)
  }
}
