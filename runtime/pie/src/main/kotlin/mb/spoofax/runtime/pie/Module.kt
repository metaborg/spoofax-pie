package mb.spoofax.runtime.pie

import com.google.inject.Binder
import com.google.inject.multibindings.MapBinder
import mb.pie.runtime.builtin.util.LogLogger
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.cache.MapCache
import mb.pie.runtime.core.impl.layer.ValidationLayer
import mb.spoofax.runtime.pie.builder.*
import mb.spoofax.runtime.pie.builder.core.*
import mb.spoofax.runtime.pie.builder.stratego.CompileStratego

open class SpoofaxPieModule : PieModule() {
  override fun configure(binder: Binder) {
    super.configure(binder)

    binder.bindPie()
  }

  override fun Binder.bindCache() {
    bind<Cache>().to<MapCache>()
  }

  override fun Binder.bindLogger() {
    bind<Logger>().to<LogLogger>()
  }

  override fun Binder.bindLayer() {
    bind<Layer>().to<ValidationLayer>()
  }

  override fun Binder.bindFuncs(builders: MapBinder<String, UFunc>) {
    bindFunc<GenerateLangSpecConfig>(builders, GenerateLangSpecConfig.id)
    bindFunc<GenerateWorkspaceConfig>(builders, GenerateWorkspaceConfig.id)

    bindFunc<GenerateTable>(builders, GenerateTable.id)
    bindFunc<GenerateSignatures>(builders, GenerateSignatures.id)
    bindFunc<Parse>(builders, Parse.id)

    bindFunc<GenerateStylerRules>(builders, GenerateStylerRules.id)
    bindFunc<Style>(builders, Style.id)

    bindFunc<NaBL2GenerateConstraintGenerator>(builders, NaBL2GenerateConstraintGenerator.id)
    bindFunc<NaBL2InitialResult>(builders, NaBL2InitialResult.id)
    bindFunc<NaBL2UnitResult>(builders, NaBL2UnitResult.id)
    bindFunc<NaBL2PartialSolve>(builders, NaBL2PartialSolve.id)
    bindFunc<NaBL2Solve>(builders, NaBL2Solve.id)

    bindFunc<CompileStratego>(builders, CompileStratego.id)

    bindFunc<CoreLoadLang>(builders, CoreLoadLang.id)
    bindFunc<CoreLoadProj>(builders, CoreLoadProj.id)
    bindFunc<CoreParse>(builders, CoreParse.id)
    bindFunc<CoreParseAll>(builders, CoreParseAll.id)
    bindFunc<CoreAnalyze>(builders, CoreAnalyze.id)
    bindFunc<CoreAnalyzeAll>(builders, CoreAnalyzeAll.id)
    bindFunc<CoreTrans>(builders, CoreTrans.id)
    bindFunc<CoreTransAll>(builders, CoreTransAll.id)
    bindFunc<CoreBuild>(builders, CoreBuild.id)
    bindFunc<CoreBuildLangSpec>(builders, CoreBuildLangSpec.id)
    bindFunc<CoreBuildOrLoad>(builders, CoreBuildOrLoad.id)
    bindFunc<CoreExtensions>(builders, CoreExtensions.id)
    bindFunc<CoreStyle>(builders, CoreStyle.id)
  }

  open protected fun Binder.bindPie() {
    bind<PieSrv>().to<PieSrvImpl>().asSingleton()
  }
}