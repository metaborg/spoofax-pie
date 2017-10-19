package mb.spoofax.runtime.pie

import com.google.inject.Binder
import com.google.inject.multibindings.MapBinder
import mb.pie.runtime.builtin.util.LogBuildLogger
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.cache.MapBuildCache
import mb.pie.runtime.core.impl.layer.ValidationBuildLayer
import mb.spoofax.runtime.pie.builder.*
import mb.spoofax.runtime.pie.builder.core.*
import mb.spoofax.runtime.pie.builder.stratego.CompileStratego

open class SpoofaxPieModule : PieModule() {
  override fun configure(binder: Binder) {
    super.configure(binder)

    binder.bindPie()
  }

  override fun Binder.bindCache() {
    bind<BuildCache>().to<MapBuildCache>()
  }

  override fun Binder.bindLogger() {
    bind<BuildLogger>().to<LogBuildLogger>()
  }

  override fun Binder.bindLayer() {
    bind<BuildLayer>().to<ValidationBuildLayer>()
  }

  override fun Binder.bindBuilders(builders: MapBinder<String, UBuilder>) {
    bindBuilder<GenerateLangSpecConfig>(builders, GenerateLangSpecConfig.id)
    bindBuilder<GenerateWorkspaceConfig>(builders, GenerateWorkspaceConfig.id)

    bindBuilder<GenerateTable>(builders, GenerateTable.id)
    bindBuilder<GenerateSignatures>(builders, GenerateSignatures.id)
    bindBuilder<Parse>(builders, Parse.id)

    bindBuilder<GenerateStylerRules>(builders, GenerateStylerRules.id)
    bindBuilder<Style>(builders, Style.id)

    bindBuilder<NaBL2GenerateConstraintGenerator>(builders, NaBL2GenerateConstraintGenerator.id)
    bindBuilder<NaBL2InitialResult>(builders, NaBL2InitialResult.id)
    bindBuilder<NaBL2UnitResult>(builders, NaBL2UnitResult.id)
    bindBuilder<NaBL2PartialSolve>(builders, NaBL2PartialSolve.id)
    bindBuilder<NaBL2Solve>(builders, NaBL2Solve.id)

    bindBuilder<CompileStratego>(builders, CompileStratego.id)

    bindBuilder<CoreLoadLang>(builders, CoreLoadLang.id)
    bindBuilder<CoreLoadProj>(builders, CoreLoadProj.id)
    bindBuilder<CoreParse>(builders, CoreParse.id)
    bindBuilder<CoreParseAll>(builders, CoreParseAll.id)
    bindBuilder<CoreAnalyze>(builders, CoreAnalyze.id)
    bindBuilder<CoreAnalyzeAll>(builders, CoreAnalyzeAll.id)
    bindBuilder<CoreTrans>(builders, CoreTrans.id)
    bindBuilder<CoreTransAll>(builders, CoreTransAll.id)
    bindBuilder<CoreBuild>(builders, CoreBuild.id)
    bindBuilder<CoreBuildLangSpec>(builders, CoreBuildLangSpec.id)
    bindBuilder<CoreBuildOrLoad>(builders, CoreBuildOrLoad.id)
    bindBuilder<CoreExtensions>(builders, CoreExtensions.id)
    bindBuilder<CoreStyle>(builders, CoreStyle.id)
  }

  open protected fun Binder.bindPie() {
    bind<PieSrv>().to<PieSrvImpl>().asSingleton()
  }
}