package mb.pipe.run.ceres.spoofax

import mb.ceres.BuildContext
import mb.ceres.Builder
import mb.pipe.run.ceres.path.read
import mb.pipe.run.ceres.spoofax.core.CoreParse
import mb.pipe.run.ceres.spoofax.core.loadLang
import mb.pipe.run.ceres.spoofax.core.parse
import mb.pipe.run.core.path.PPath
import mb.pipe.run.spoofax.cfg.LangSpecConfig
import java.io.Serializable

class GenerateLangSpecConfig : Builder<GenerateLangSpecConfig.Input, LangSpecConfig?> {
  companion object {
    val id = "spoofaxGenerateLangSpecConfig"
  }

  data class Input(val langLoc: PPath, val file: PPath) : Serializable

  override val id: String = Companion.id
  override fun BuildContext.build(input: Input): LangSpecConfig? {
    val text = read(input.file)
    val langImpl = loadLang(input.langLoc)
    val (ast, _, _) = parse(CoreParse.Input(langImpl.id(), input.file, text))
    if (ast == null) {
      return null
    }
    val dir = input.file.parent();
    val config = LangSpecConfig(ast, dir)
    return config
  }
}