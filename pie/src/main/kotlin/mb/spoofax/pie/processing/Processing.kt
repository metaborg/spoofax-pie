package mb.spoofax.pie.processing

import com.google.inject.Inject
import mb.pie.api.*
import mb.pie.api.stamp.FileStampers
import mb.pie.vfs.path.PPath
import mb.spoofax.pie.config.ParseWorkspaceConfig
import mb.spoofax.pie.legacy.*
import mb.spoofax.runtime.cfg.LangId
import org.metaborg.core.language.ResourceExtensionFacet
import org.metaborg.spoofax.meta.core.build.SpoofaxLangSpecCommonPaths
import java.io.Serializable

fun shouldProcessDocument(document: PPath): Boolean {
  val str = document.toString()
  return !str.contains("src-gen") && !str.contains("target")
}

class LangSpecExtensions @Inject constructor(
  private val parseWorkspaceConfig: ParseWorkspaceConfig
) : TaskDef<PPath, ArrayList<String>> {
  companion object {
    const val id = "processing.LangSpecExtensions"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): ArrayList<String> {
    return with(parseWorkspaceConfig) {
      requireConfigValue(input) { workspaceConfig ->
        ArrayList(workspaceConfig.extensions())
      }
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
      require(SpoofaxLangSpecCommonPaths(it.location()).targetMetaborgDir().resolveFile("editor.esv.af").pPath, FileStampers.hash)
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

  data class Input(val document: PPath, val root: PPath) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): Boolean {
    val (document, root) = input
    val extension = document.extension()
      ?: throw ExecException("Cannot determine if document $document is a Spoofax-PIE document; it has no extension")
    return with(parseWorkspaceConfig) {
      requireConfigValue(root) { workspaceConfig ->
        val langSpecConfig = workspaceConfig.langSpecConfigForExt(extension)
        langSpecConfig != null
      }
    }
  }
}

class LangIdOfDocument @Inject constructor(
  private val parseWorkspaceConfig: ParseWorkspaceConfig
) : TaskDef<LangIdOfDocument.Input, LangId> {
  companion object {
    const val id = "processing.LangIdOfDocument"
  }

  data class Input(val document: PPath, val root: PPath) : Serializable

  override val id = Companion.id
  override fun ExecContext.exec(input: Input): LangId {
    val (document, root) = input
    val extension = document.extension()
      ?: throw ExecException("Cannot determine if document $document is a Spoofax-PIE document; it has no extension")
    return with(parseWorkspaceConfig) {
      requireConfigValue(root) { workspaceConfig ->
        val langSpecConfig = workspaceConfig.langSpecConfigForExt(extension)
        langSpecConfig?.id()
      }
    }
      ?: throw ExecException("Cannot determine if document $document is a Spoofax-PIE document; no language specification was found for extension $extension")
  }
}

class IsLegacyDocument : TaskDef<PPath, Boolean> {
  companion object {
    const val id = "processing.IsLegacyDocument"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): Boolean {
    val spoofax = Spx.spoofax()
    return spoofax.languageIdentifierService.identify(input.fileObject) != null
  }
}