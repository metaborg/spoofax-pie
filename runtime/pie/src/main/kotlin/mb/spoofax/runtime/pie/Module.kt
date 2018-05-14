package mb.spoofax.runtime.pie

import com.google.inject.Binder
import com.google.inject.multibindings.MapBinder
import mb.pie.runtime.builtin.util.LogLogger
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.cache.MapCache
import mb.pie.runtime.core.impl.layer.ValidationLayer
import mb.spoofax.runtime.pie.config.ParseWorkspaceCfg
import mb.spoofax.runtime.pie.config.ParseLangSpecCfg
import mb.spoofax.runtime.pie.esv.CompileStyler
import mb.spoofax.runtime.pie.esv.Style
import mb.spoofax.runtime.pie.legacy.*
import mb.spoofax.runtime.pie.nabl2.*
import mb.spoofax.runtime.pie.sdf3.*
import mb.spoofax.runtime.pie.stratego.Compile

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
    bindFunc<ParseLangSpecCfg>(builders, ParseLangSpecCfg.id)
    bindFunc<ParseWorkspaceCfg>(builders, ParseWorkspaceCfg.id)

    bindFunc<CompileParseTable>(builders, CompileParseTable.id)
    bindFunc<GenerateStrategoSignatures>(builders, GenerateStrategoSignatures.id)
    bindFunc<Parse>(builders, Parse.id)

    bindFunc<CompileStyler>(builders, CompileStyler.id)
    bindFunc<Style>(builders, Style.id)

    bindFunc<GenerateStrategoCGen>(builders, GenerateStrategoCGen.id)
    bindFunc<CompileStrategoCGen>(builders, CompileStrategoCGen.id)
    bindFunc<CGenGlobal>(builders, CGenGlobal.id)
    bindFunc<CGenDocument>(builders, CGenDocument.id)
    bindFunc<SolveGlobal>(builders, SolveGlobal.id)
    bindFunc<SolveDocument>(builders, SolveDocument.id)
    bindFunc<SolveFinal>(builders, SolveFinal.id)

    bindFunc<Compile>(builders, Compile.id)

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