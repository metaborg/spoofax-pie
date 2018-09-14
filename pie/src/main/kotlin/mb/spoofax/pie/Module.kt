package mb.spoofax.pie

import com.google.inject.*
import com.google.inject.multibindings.MapBinder
import mb.pie.api.TaskDef
import mb.pie.taskdefs.guice.TaskDefsModule
import mb.pie.vfs.path.PathSrv
import mb.pie.vfs.path.PathSrvImpl
import mb.spoofax.pie.config.ParseWorkspaceConfig
import mb.spoofax.pie.analysis.*
import mb.spoofax.pie.esv.ESVToStylingRules
import mb.spoofax.pie.jsglr.JSGLRParse
import mb.spoofax.pie.legacy.*
import mb.spoofax.pie.analysis.CompileAnalyzer
import mb.spoofax.pie.nabl2.NaBL2ToStrategoAnalyzer
import mb.spoofax.pie.processing.*
import mb.spoofax.pie.sdf3.SDF3ToJSGLRParseTable
import mb.spoofax.pie.sdf3.SDF3ToStrategoSignatures
import mb.spoofax.pie.stratego.CompileStratego
import mb.spoofax.pie.style.SpoofaxStyle

open class SpoofaxPieTaskDefsModule : TaskDefsModule() {
  override fun Binder.bindTaskDefs(taskDefsBinder: MapBinder<String, TaskDef<*, *>>) {
    // Config
    bindTaskDef<ParseWorkspaceConfig>(taskDefsBinder, ParseWorkspaceConfig.id)

    // Runtime
    // Processing
    bindTaskDef<LangSpecExtensions>(taskDefsBinder, LangSpecExtensions.id)
    bindTaskDef<LegacyExtensions>(taskDefsBinder, LegacyExtensions.id)
    bindTaskDef<IsLangSpecDocument>(taskDefsBinder, IsLangSpecDocument.id)
    bindTaskDef<LangIdOfDocument>(taskDefsBinder, LangIdOfDocument.id)
    bindTaskDef<IsLegacyDocument>(taskDefsBinder, IsLegacyDocument.id)
    // Spoofax styling
    bindTaskDef<SpoofaxStyle>(taskDefsBinder, SpoofaxStyle.id)
    // JSGLR
    bindTaskDef<JSGLRParse>(taskDefsBinder, JSGLRParse.id)
    // Analysis
    bindTaskDef<CompileAnalyzer>(taskDefsBinder, CompileAnalyzer.id)
    bindTaskDef<AnalyzeContainer>(taskDefsBinder, AnalyzeContainer.id)
    bindTaskDef<AnalyzeDocument>(taskDefsBinder, AnalyzeDocument.id)
    bindTaskDef<AnalyzeFinal>(taskDefsBinder, AnalyzeFinal.id)

    // Meta-language
    // ESV
    bindTaskDef<ESVToStylingRules>(taskDefsBinder, ESVToStylingRules.id)
    // Stratego
    bindTaskDef<CompileStratego>(taskDefsBinder, CompileStratego.id)
    // SDF3
    bindTaskDef<SDF3ToJSGLRParseTable>(taskDefsBinder, SDF3ToJSGLRParseTable.id)
    bindTaskDef<SDF3ToStrategoSignatures>(taskDefsBinder, SDF3ToStrategoSignatures.id)
    // NaBL2
    bindTaskDef<NaBL2ToStrategoAnalyzer>(taskDefsBinder, NaBL2ToStrategoAnalyzer.id)

    // Legacy
    bindTaskDef<LegacyLoadProject>(taskDefsBinder, LegacyLoadProject.id)
    bindTaskDef<LegacyParse>(taskDefsBinder, LegacyParse.id)
    bindTaskDef<LegacyParseAll>(taskDefsBinder, LegacyParseAll.id)
    bindTaskDef<LegacyStyle>(taskDefsBinder, LegacyStyle.id)
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

