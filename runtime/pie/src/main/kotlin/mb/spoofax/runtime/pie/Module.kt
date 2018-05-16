package mb.spoofax.runtime.pie

import com.google.inject.Binder
import com.google.inject.multibindings.MapBinder
import mb.pie.api.*
import mb.pie.logger.mblog.LogLogger
import mb.pie.runtime.cache.MapCache
import mb.pie.runtime.layer.ValidationLayer
import mb.pie.taskdefs.guice.bindTaskDef
import mb.spoofax.runtime.pie.config.ParseLangSpecCfg
import mb.spoofax.runtime.pie.config.ParseWorkspaceCfg
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

  override fun Binder.bindTaskDefs(builders: MapBinder<String, UTaskDef>) {
    bindTaskDef<ParseLangSpecCfg>(builders, ParseLangSpecCfg.id)
    bindTaskDef<ParseWorkspaceCfg>(builders, ParseWorkspaceCfg.id)

    bindTaskDef<CompileParseTable>(builders, CompileParseTable.id)
    bindTaskDef<GenerateStrategoSignatures>(builders, GenerateStrategoSignatures.id)
    bindTaskDef<Parse>(builders, Parse.id)

    bindTaskDef<CompileStyler>(builders, CompileStyler.id)
    bindTaskDef<Style>(builders, Style.id)

    bindTaskDef<GenerateStrategoCGen>(builders, GenerateStrategoCGen.id)
    bindTaskDef<CompileStrategoCGen>(builders, CompileStrategoCGen.id)
    bindTaskDef<CGenGlobal>(builders, CGenGlobal.id)
    bindTaskDef<CGenDocument>(builders, CGenDocument.id)
    bindTaskDef<SolveGlobal>(builders, SolveGlobal.id)
    bindTaskDef<SolveDocument>(builders, SolveDocument.id)
    bindTaskDef<SolveFinal>(builders, SolveFinal.id)

    bindTaskDef<Compile>(builders, Compile.id)

    bindTaskDef<CoreLoadLang>(builders, CoreLoadLang.id)
    bindTaskDef<CoreLoadProj>(builders, CoreLoadProj.id)
    bindTaskDef<CoreParse>(builders, CoreParse.id)
    bindTaskDef<CoreParseAll>(builders, CoreParseAll.id)
    bindTaskDef<CoreAnalyze>(builders, CoreAnalyze.id)
    bindTaskDef<CoreAnalyzeAll>(builders, CoreAnalyzeAll.id)
    bindTaskDef<CoreTrans>(builders, CoreTrans.id)
    bindTaskDef<CoreTransAll>(builders, CoreTransAll.id)
    bindTaskDef<CoreBuild>(builders, CoreBuild.id)
    bindTaskDef<CoreBuildLangSpec>(builders, CoreBuildLangSpec.id)
    bindTaskDef<CoreBuildOrLoad>(builders, CoreBuildOrLoad.id)
    bindTaskDef<CoreExtensions>(builders, CoreExtensions.id)
    bindTaskDef<CoreStyle>(builders, CoreStyle.id)
  }

  open protected fun Binder.bindPie() {
    bind<PieSrv>().to<PieSrvImpl>().asSingleton()
  }
}