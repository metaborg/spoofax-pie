package mb.spoofax.runtime.pie.builder

import mb.pie.runtime.builtin.path.read
import mb.spoofax.runtime.pie.builder.core.langExtensions
import mb.spoofax.runtime.pie.builder.core.parse
import mb.pie.runtime.core.BuildContext
import mb.pie.runtime.core.Builder
import mb.spoofax.runtime.impl.cfg.LangSpecConfig
import mb.spoofax.runtime.impl.cfg.SpxCoreConfig
import mb.spoofax.runtime.impl.cfg.WorkspaceConfig
import mb.vfs.path.PPath
import java.io.Serializable

class GenerateLangSpecConfig : Builder<GenerateLangSpecConfig.Input, LangSpecConfig?> {
  companion object {
    val id = "spoofaxGenerateLangSpecConfig"
  }

  data class Input(val config: SpxCoreConfig, val file: PPath) : Serializable

  override val id: String = Companion.id
  override fun BuildContext.build(input: Input): LangSpecConfig? {
    val text = read(input.file)
    val (ast, _, _) = parse(input.config, text)
    if (ast == null) {
      return null
    }
    val dir = input.file.parent();
    val config = LangSpecConfig(ast, dir)
    return config
  }
}

class GenerateWorkspaceConfig : Builder<GenerateWorkspaceConfig.Input, WorkspaceConfig?> {
  companion object {
    val id = "spoofaxGenerateWorkspaceConfig"
  }

  data class Input(val text: String, val workspaceRoot: PPath, val config: SpxCoreConfig) : Serializable

  override val id: String = Companion.id
  override fun BuildContext.build(input: Input): WorkspaceConfig? {
    val (ast, _, _) = parse(input.config, input.text)
    if (ast == null) {
      return null
    }
    val data = WorkspaceConfig.WorkspaceConfigPaths(ast, input.workspaceRoot)
    val langSpecConfigs = data.langSpecConfigFiles.mapNotNull {
      requireOutput(GenerateLangSpecConfig::class.java, GenerateLangSpecConfig.Input(input.config, it))
    }
    val spxCoreLangConfigs = data.spxCoreLangConfigFiles.map {
      val langDir = it.parent()!!
      val extensions = langExtensions(langDir, false)
      SpxCoreConfig(langDir, false, extensions)
    }
    val spxCoreLangSpecConfigs = data.spxCoreLangSpecConfigFiles.map {
      val langDir = it.parent()!!
      val extensions = langExtensions(langDir, true)
      SpxCoreConfig(langDir, true, extensions)
    }
    val config = WorkspaceConfig(langSpecConfigs, spxCoreLangConfigs + spxCoreLangSpecConfigs)
    return config
  }
}