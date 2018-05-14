package mb.spoofax.runtime.pie.config

import mb.pie.runtime.builtin.path.read
import mb.pie.runtime.core.ExecContext
import mb.pie.runtime.core.Func
import mb.spoofax.runtime.impl.cfg.LangSpecConfig
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.spoofax.runtime.pie.legacy.parse
import mb.vfs.path.PPath
import java.io.Serializable

class ParseLangSpecCfg : Func<ParseLangSpecCfg.Input, LangSpecConfig?> {
  companion object {
    const val id = "config.ParseLangSpecCfg"
  }

  data class Input(val configLangCfg: SpxCoreConfig, val file: PPath) : Serializable

  override val id: String = Companion.id
  override fun ExecContext.exec(input: Input): LangSpecConfig? {
    val file = input.file
    val text = read(file) ?: return null
    val (ast, _, _) = parse(input.configLangCfg, text, file)
    if(ast == null) {
      return null
    }
    val dir = file.parent();
    val config = LangSpecConfig.fromTerm(ast, dir)
    return config

  }
}
