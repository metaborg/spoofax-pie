package mb.pipe.run.ceres

import com.google.inject.Binder
import com.google.inject.Module
import mb.ceres.bindBuilder
import mb.ceres.builderMapBinder
import mb.pipe.run.ceres.path.Copy
import mb.pipe.run.ceres.path.Read
import mb.pipe.run.ceres.spoofax.GenerateStylerRules
import mb.pipe.run.ceres.spoofax.GenerateTable
import mb.pipe.run.ceres.spoofax.Parse
import mb.pipe.run.ceres.spoofax.Style
import mb.pipe.run.ceres.spoofax.legacy.*

class PipeCeresModule : Module {
  override fun configure(binder: Binder) {
    val builders = binder.builderMapBinder()

    binder.bindBuilder<Read>(builders, "read")
    binder.bindBuilder<Copy>(builders, "copy")

    binder.bindBuilder<GenerateTable>(builders, "generateTable")
    binder.bindBuilder<Parse>(builders, "spoofaxParse")

    binder.bindBuilder<GenerateStylerRules>(builders, "generateStylerRules")
    binder.bindBuilder<Style>(builders, "spoofaxStyle")

    binder.bindBuilder<CoreLoadLang>(builders, "coreLoadLang")
    binder.bindBuilder<CoreLoadProj>(builders, "coreLoadProj")
    binder.bindBuilder<CoreParse>(builders, "coreParse")
    binder.bindBuilder<CoreAnalyze>(builders, "coreAnalyze")
    binder.bindBuilder<CoreTrans>(builders, "coreTrans")

  }
}