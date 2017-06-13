package mb.pipe.run.ceres

import com.google.inject.Binder
import com.google.inject.Module
import mb.ceres.asSingleton
import mb.ceres.bind
import mb.ceres.bindBuilder
import mb.ceres.builderMapBinder
import mb.ceres.impl.BuildCache
import mb.ceres.impl.MapBuildCache
import mb.ceres.to
import mb.pipe.run.ceres.path.Copy
import mb.pipe.run.ceres.path.Read
import mb.pipe.run.ceres.spoofax.GenerateStylerRules
import mb.pipe.run.ceres.spoofax.GenerateTable
import mb.pipe.run.ceres.spoofax.Parse
import mb.pipe.run.ceres.spoofax.Style
import mb.pipe.run.ceres.spoofax.legacy.CoreAnalyze
import mb.pipe.run.ceres.spoofax.legacy.CoreLoadLang
import mb.pipe.run.ceres.spoofax.legacy.CoreLoadProj
import mb.pipe.run.ceres.spoofax.legacy.CoreParse
import mb.pipe.run.ceres.spoofax.legacy.CoreTrans

open class PipeCeresModule : Module {
  override fun configure(binder: Binder) {
    binder.bindCache();
    binder.bindCeres();
    binder.bindBuilders();

  }

  open protected fun Binder.bindCache() {
    bind<BuildCache>().to<MapBuildCache>();
  }

  open protected fun Binder.bindCeres() {
    bind<CeresSrv>().to<CeresSrvImpl>().asSingleton();
  }

  open protected fun Binder.bindBuilders() {
    val builders = builderMapBinder()

    bindBuilder<Read>(builders, "read")
    bindBuilder<Copy>(builders, "copy")

    bindBuilder<GenerateTable>(builders, "generateTable")
    bindBuilder<Parse>(builders, "spoofaxParse")

    bindBuilder<GenerateStylerRules>(builders, "generateStylerRules")
    bindBuilder<Style>(builders, "spoofaxStyle")

    bindBuilder<CoreLoadLang>(builders, "coreLoadLang")
    bindBuilder<CoreLoadProj>(builders, "coreLoadProj")
    bindBuilder<CoreParse>(builders, "coreParse")
    bindBuilder<CoreAnalyze>(builders, "coreAnalyze")
    bindBuilder<CoreTrans>(builders, "coreTrans")
  }
}