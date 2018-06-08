package mb.spoofax.pie.config

import com.google.inject.Inject
import mb.pie.api.ExecContext
import mb.pie.api.TaskDef
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.legacy.LegacyLanguageExtensions
import mb.spoofax.pie.legacy.LegacyParse
import mb.spoofax.runtime.cfg.*
import java.io.Serializable

class ParseWorkspaceCfg @Inject constructor(
  private val legacyParse: LegacyParse,
  private val legacyLanguageExtensions: LegacyLanguageExtensions,
  private val parseLangSpecCfg: ParseLangSpecCfg
) : TaskDef<ParseWorkspaceCfg.Input, WorkspaceConfig?> {
  companion object {
    const val id = "config.ParseWorkspaceCfg"
  }

  data class Input(val text: String, val file: PPath, val workspaceRoot: PPath, val config: SpxCoreConfig) : Serializable
  data class Key(val file: PPath, val workspaceRoot: PPath) : Serializable {
    constructor(input: Input) : this(input.file, input.workspaceRoot)
  }

  override val id: String = Companion.id
  override fun key(input: Input) = Key(input)
  override fun ExecContext.exec(input: Input): WorkspaceConfig? {
    val (text, file, workspaceRoot, config) = input
    val (ast, _, _) = require(legacyParse.createTask(config, text, file))
    if(ast == null) {
      return null
    }
    val data = WorkspaceConfigPaths.fromTerm(ast, input.workspaceRoot)
    val langSpecConfigs = data.langSpecConfigFiles().mapNotNull {
      require(parseLangSpecCfg, ParseLangSpecCfg.Input(input.config, it))
    }
    val spxCoreLangConfigs = data.spxCoreLangConfigFiles().map {
      val langDir = it.parent()!!
      val extensions = require(legacyLanguageExtensions, LegacyLanguageExtensions.Input(langDir, false))
      ImmutableSpxCoreConfig.of(langDir, false, extensions)
    }
    val spxCoreLangSpecConfigs = data.spxCoreLangSpecConfigFiles().map {
      val langDir = it.parent()!!
      val extensions = require(legacyLanguageExtensions, LegacyLanguageExtensions.Input(langDir, true))
      ImmutableSpxCoreConfig.of(langDir, true, extensions)
    }
    return WorkspaceConfig.fromConfigs(langSpecConfigs, spxCoreLangConfigs + spxCoreLangSpecConfigs)
  }
}
