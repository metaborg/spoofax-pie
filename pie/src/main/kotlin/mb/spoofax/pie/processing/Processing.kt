package mb.spoofax.pie.processing

import com.google.inject.Inject
import mb.fs.java.JavaFSPath
import mb.pie.api.*
import mb.pie.api.fs.stamp.FileSystemStampers
import mb.spoofax.pie.config.ParseWorkspaceConfig
import mb.spoofax.pie.config.requireConfigValue
import mb.spoofax.pie.legacy.*
import mb.spoofax.runtime.cfg.LangId
import mb.spoofax.runtime.cfg.WorkspaceConfig
import org.metaborg.core.language.ResourceExtensionFacet
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths
import java.io.Serializable

fun shouldProcessDocument(document: JavaFSPath): Boolean {
  val str = document.toString()
  return !str.contains("src-gen") && !str.contains("target")
}

class LangSpecExtensions @Inject constructor(
  private val parseWorkspaceConfig: ParseWorkspaceConfig
) : TaskDef<JavaFSPath, ArrayList<String>> {
  companion object {
    const val id = "processing.LangSpecExtensions"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: JavaFSPath): ArrayList<String> {
    return requireConfigValue(this, parseWorkspaceConfig, input) { workspaceConfig ->
      ArrayList(workspaceConfig.extensions())
    }
  }
}

class LegacyExtensions : TaskDef<None, ArrayList<String>> {
  companion object {
    const val id = "processing.LegacyExtensions"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: None): ArrayList<String> {
    val spoofax = Spx.spoofax()
    spoofax.languageService.allComponents.forEach {
      require(SpoofaxLangSpecCommonPaths(it.location()).targetMetaborgDir().resolveFile("editor.esv.af").fsPath, FileSystemStampers.hash)
    }
    return spoofax.languageService.allComponents
      .map {
        val facet = it.facet(ResourceExtensionFacet::class.java)
        if(facet != null) {
          facet.extensions()
        } else {
          listOf<String>()
        }
      }
      .flatMapTo(ArrayList()) { it }
  }
}

class IsLangSpecDocument @Inject constructor(
  private val parseWorkspaceConfig: ParseWorkspaceConfig
) : TaskDef<IsLangSpecDocument.Input, Boolean> {
  companion object {
    const val id = "processing.IsLangSpecDocument"
  }

  data class Input(val document: JavaFSPath, val root: JavaFSPath) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Boolean {
    val (document, root) = input
    val extension = document.leafExtension
      ?: throw ExecException("Cannot determine if document $document is a Spoofax-PIE document; it has no extension")
    return requireConfigValue(this, parseWorkspaceConfig, root) { workspaceConfig ->
      val langSpecConfig = workspaceConfig.langSpecConfigForExt(extension)
      langSpecConfig != null
    }
  }
}

class LangIdOfDocument @Inject constructor(
  private val parseWorkspaceConfig: ParseWorkspaceConfig
) : TaskDef<LangIdOfDocument.Input, LangId> {
  companion object {
    const val id = "processing.LangIdOfDocument"
  }

  data class Input(val document: JavaFSPath, val root: JavaFSPath) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): LangId {
    val (document, root) = input
    val extension = document.leafExtension
      ?: throw ExecException("Cannot determine if document $document is a Spoofax-PIE document; it has no extension")
    return requireConfigValue(this, parseWorkspaceConfig, root, ConfigValueFunc(extension))
      ?: throw ExecException("Cannot determine if document $document is a Spoofax-PIE document; no language specification was found for extension $extension")
  }

  private class ConfigValueFunc(val extension: String) : (WorkspaceConfig) -> LangId?, Serializable {
    override fun invoke(workspaceConfig: WorkspaceConfig): LangId? {
      val langSpecConfig = workspaceConfig.langSpecConfigForExt(extension)
      return langSpecConfig?.id()
    }
  }
}

class IsLegacyDocument : TaskDef<JavaFSPath, Boolean> {
  companion object {
    const val id = "processing.IsLegacyDocument"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: JavaFSPath): Boolean {
    val spoofax = Spx.spoofax()
    return spoofax.languageIdentifierService.identify(input.fileObject) != null
  }
}