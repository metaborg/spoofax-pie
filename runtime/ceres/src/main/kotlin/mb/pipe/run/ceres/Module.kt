package mb.pipe.run.ceres

import com.google.inject.Binder
import mb.ceres.BuildReporter
import mb.ceres.CeresModule
import mb.ceres.asSingleton
import mb.ceres.bind
import mb.ceres.bindBuilder
import mb.ceres.builderMapBinder
import mb.ceres.impl.BuildCache
import mb.ceres.impl.MapBuildCache
import mb.ceres.to
import mb.pipe.run.ceres.path.Copy
import mb.pipe.run.ceres.path.Exists
import mb.pipe.run.ceres.path.ListContents
import mb.pipe.run.ceres.path.Read
import mb.pipe.run.ceres.path.WalkContents
import mb.pipe.run.ceres.spoofax.GenerateLangSpecConfig
import mb.pipe.run.ceres.spoofax.GenerateStylerRules
import mb.pipe.run.ceres.spoofax.GenerateTable
import mb.pipe.run.ceres.spoofax.GenerateWorkspaceConfig
import mb.pipe.run.ceres.spoofax.Parse
import mb.pipe.run.ceres.spoofax.Style
import mb.pipe.run.ceres.spoofax.core.CoreAnalyze
import mb.pipe.run.ceres.spoofax.core.CoreBuild
import mb.pipe.run.ceres.spoofax.core.CoreBuildLangSpec
import mb.pipe.run.ceres.spoofax.core.CoreBuildOrLoad
import mb.pipe.run.ceres.spoofax.core.CoreExtensions
import mb.pipe.run.ceres.spoofax.core.CoreLoadLang
import mb.pipe.run.ceres.spoofax.core.CoreLoadProj
import mb.pipe.run.ceres.spoofax.core.CoreParse
import mb.pipe.run.ceres.spoofax.core.CoreStyle
import mb.pipe.run.ceres.spoofax.core.CoreTrans
import mb.pipe.run.ceres.util.LoggerBuildReporter

open class PipeCeresModule : CeresModule() {
  override fun configure(binder: Binder) {
    super.configure(binder)

    binder.bindCache()
    binder.bindCeres()
    binder.bindBuilders()
  }

  open protected fun Binder.bindCache() {
    bind<BuildCache>().to<MapBuildCache>()
  }

  override fun Binder.bindReporter() {
    bind<BuildReporter>().to<LoggerBuildReporter>()
  }

  open protected fun Binder.bindCeres() {
    bind<CeresSrv>().to<CeresSrvImpl>().asSingleton()
  }

  open protected fun Binder.bindBuilders() {
    val builders = builderMapBinder()

    bindBuilder<Exists>(builders, Exists.id)
    bindBuilder<ListContents>(builders, ListContents.id)
    bindBuilder<WalkContents>(builders, WalkContents.id)
    bindBuilder<Read>(builders, Read.id)
    bindBuilder<Copy>(builders, Copy.id)

    bindBuilder<GenerateLangSpecConfig>(builders, GenerateLangSpecConfig.id)
    bindBuilder<GenerateWorkspaceConfig>(builders, GenerateWorkspaceConfig.id)

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
    bindBuilder<CoreBuildOrLoad>(builders, CoreBuildOrLoad.id)
    bindBuilder<CoreExtensions>(builders, CoreExtensions.id)
    bindBuilder<CoreStyle>(builders, CoreStyle.id)
  }
}