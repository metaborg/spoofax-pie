package mb.pipe.run.ceres

import com.google.inject.Binder
import com.google.inject.Module
import mb.ceres.*
import mb.ceres.impl.BuildCache
import mb.ceres.impl.MapBuildCache
import mb.pipe.run.ceres.path.*
import mb.pipe.run.ceres.spoofax.*
import mb.pipe.run.ceres.spoofax.core.*

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

    bindBuilder<Exists>(builders, Exists.id)
    bindBuilder<ListContents>(builders, ListContents.id)
    bindBuilder<WalkContents>(builders, WalkContents.id)
    bindBuilder<Read>(builders, Read.id)
    bindBuilder<Copy>(builders, Copy.id)

    bindBuilder<GenerateLangSpecConfig>(builders, GenerateLangSpecConfig.id)

    bindBuilder<GenerateTable>(builders, GenerateTable.id)
    bindBuilder<Parse>(builders, Parse.id)

    bindBuilder<GenerateStylerRules>(builders, GenerateStylerRules.id)
    bindBuilder<Style>(builders, Style.id)

    bindBuilder<CoreLoadLang>(builders, CoreLoadLang.id)
    bindBuilder<CoreLoadProj>(builders, CoreLoadProj.id)
    bindBuilder<CoreParse>(builders, CoreParse.id)
    bindBuilder<CoreAnalyze>(builders, CoreAnalyze.id)
    bindBuilder<CoreTrans>(builders, CoreTrans.id)
    bindBuilder<CoreBuild>(builders, CoreBuild.id)
    bindBuilder<CoreBuildLangSpec>(builders, CoreBuildLangSpec.id)
    bindBuilder<CoreExtensions>(builders, CoreExtensions.id)
    bindBuilder<CoreStyle>(builders, CoreStyle.id)
  }
}