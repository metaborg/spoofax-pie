package mb.spoofax.runtime.pie.builder

import mb.pie.runtime.builtin.path.read
import mb.pie.runtime.core.*
import mb.spoofax.runtime.impl.cfg.*
import mb.spoofax.runtime.pie.builder.core.langExtensions
import mb.spoofax.runtime.pie.builder.core.parse
import mb.vfs.path.PPath
import java.io.Serializable

class GenerateLangSpecConfig : Func<GenerateLangSpecConfig.Input, LangSpecConfig?> {
  companion object {
    val id = "spoofaxGenerateLangSpecConfig"
  }

  data class Input(val config: SpxCoreConfig, val file: PPath) : Serializable

  override val id: String = Companion.id
  override fun ExecContext.exec(input: Input): LangSpecConfig? {
    val file = input.file
    val text = read(file) ?: return null
    val (ast, _, _) = parse(input.config, text, file)
    if(ast == null) {
      return null
    }
    val dir = file.parent();
    val config = LangSpecConfig.fromTerm(ast, dir)
    return config

  }
}

class GenerateWorkspaceConfig : Func<GenerateWorkspaceConfig.Input, WorkspaceConfig?> {
  companion object {
    val id = "spoofaxGenerateWorkspaceConfig"
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
      requireOutput(GenerateLangSpecConfig::class, GenerateLangSpecConfig.Companion.id, GenerateLangSpecConfig.Input(input.config, it))
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
    val config = WorkspaceConfig.fromConfigs(langSpecConfigs, spxCoreLangConfigs + spxCoreLangSpecConfigs)
    return config
  }
}