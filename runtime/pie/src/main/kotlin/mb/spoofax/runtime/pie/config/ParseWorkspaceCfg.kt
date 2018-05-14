package mb.spoofax.runtime.pie.config

import mb.pie.runtime.core.ExecContext
import mb.pie.runtime.core.TaskDef
import mb.spoofax.runtime.impl.cfg.*
import mb.spoofax.runtime.pie.legacy.langExtensions
import mb.spoofax.runtime.pie.legacy.parse
import mb.vfs.path.PPath
import java.io.Serializable

class ParseWorkspaceCfg : TaskDef<ParseWorkspaceCfg.Input, WorkspaceConfig?> {
  companion object {
    const val id = "config.ParseWorkspaceCfg"
  }

  data class Input(val text: String, val file: PPath, val workspaceRoot: PPath, val config: SpxCoreConfig) : Serializable

  override val id: String = Companion.id
  override fun ExecContext.exec(input: Input): WorkspaceConfig? {
    val (ast, _, _) = parse(input.config, input.text, input.file)
    if(ast == null) {
      return null
    }
    val data = WorkspaceConfigPaths.fromTerm(ast, input.workspaceRoot)
    val langSpecConfigs = data.langSpecConfigFiles().mapNotNull {
      requireOutput(ParseLangSpecCfg::class.java, ParseLangSpecCfg.id, ParseLangSpecCfg.Input(input.config, it))
    }
    val spxCoreLangConfigs = data.spxCoreLangConfigFiles().map {
      val langDir = it.parent()!!
      val extensions = langExtensions(langDir, false)
      ImmutableSpxCoreConfig.of(langDir, false, extensions)
    }
    val spxCoreLangSpecConfigs = data.spxCoreLangSpecConfigFiles().map {
      val langDir = it.parent()!!
      val extensions = langExtensions(langDir, true)
      ImmutableSpxCoreConfig.of(langDir, true, extensions)
    }
    return WorkspaceConfig.fromConfigs(langSpecConfigs, spxCoreLangConfigs + spxCoreLangSpecConfigs)
  }
}
