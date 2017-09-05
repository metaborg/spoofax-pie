package mb.spoofax.runtime.pie.builder.core

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.core.BuildContext
import mb.pie.runtime.core.Builder
import mb.pie.runtime.core.OutTransient
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.vfs.path.PPath
import org.metaborg.core.language.ILanguageImpl
import java.io.Serializable

class CoreBuildOrLoad @Inject constructor(log: Logger) : Builder<CoreBuildOrLoad.Input, OutTransient<ILanguageImpl>> {
  companion object {
    val id = "coreBuildOrLoad"
  }

  data class Input(val dir: PPath, val isLangSpec: Boolean) : Serializable {
    constructor(config: SpxCoreConfig) : this(config.dir(), config.isLangSpec)
  }

  val log: Logger = log.forContext(CoreBuild::class.java)

  override val id = Companion.id
  override fun BuildContext.build(input: Input): OutTransient<ILanguageImpl> {
    val dir = input.dir;
    if (input.isLangSpec) {
      buildProject(dir)
      buildLangSpec(dir)
    }
    return loadLangRaw(dir)
  }
}

fun BuildContext.buildOrLoad(input: CoreBuildOrLoad.Input) = requireOutput(CoreBuildOrLoad::class.java, input).v
fun BuildContext.buildOrLoad(dir: PPath, isLangSpec: Boolean) = buildOrLoad(CoreBuildOrLoad.Input(dir, isLangSpec))
fun BuildContext.buildOrLoad(input: SpxCoreConfig) = buildOrLoad(CoreBuildOrLoad.Input(input))
