package mb.spoofax.pie

import com.google.inject.*
import com.google.inject.multibindings.MapBinder
import mb.pie.api.UTaskDef
import mb.pie.taskdefs.guice.TaskDefsModule
import mb.pie.taskdefs.guice.bindTaskDef
import mb.pie.vfs.path.PathSrv
import mb.pie.vfs.path.PathSrvImpl
import mb.spoofax.pie.config.ParseLangSpecCfg
import mb.spoofax.pie.config.ParseWorkspaceCfg
import mb.spoofax.pie.esv.CompileStyler
import mb.spoofax.pie.esv.Style
import mb.spoofax.pie.legacy.*
import mb.spoofax.pie.nabl2.*
import mb.spoofax.pie.sdf3.*
import mb.spoofax.pie.stratego.Compile

open class SpoofaxPieTaskDefsModule : TaskDefsModule() {
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
}

open class SpoofaxPieModule : AbstractModule() {
  override fun configure() {
    bind(SpoofaxPipeline::class.java).`in`(Singleton::class.java)
  }
}

open class PieVfsModule : AbstractModule() {
  override fun configure() {
    bind(PathSrv::class.java).to(PathSrvImpl::class.java).`in`(Singleton::class.java)
  }
}

