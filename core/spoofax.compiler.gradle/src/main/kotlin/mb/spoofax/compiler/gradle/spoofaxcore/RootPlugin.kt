package mb.spoofax.compiler.gradle.spoofaxcore

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.IOException
import java.util.*

open class RootPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val persistentPropertiesFile = project.buildDir.resolve("spoofaxCompiler/persistent.properties")
    val persistentProperties = Properties()
    if(persistentPropertiesFile.exists()) {
      persistentPropertiesFile.inputStream().use {
        try {
          persistentProperties.load(it)
        } catch(e: IOException) {
          project.logger.warn("Failed to load persistent properties", e)
        }
      }
    }

    val extension = RootProjectExtension(project.objects, project.projectDir, persistentProperties)
    project.extensions.add(RootProjectExtension.id, extension)
    project.subprojects {
      extensions.add(RootProjectExtension.id, extension)
    }

    project.afterEvaluate {
      extension.shared.savePersistentProperties(persistentProperties)
      persistentPropertiesFile.parentFile.mkdirs()
      persistentPropertiesFile.createNewFile()
      persistentPropertiesFile.outputStream().use {
        try {
          persistentProperties.store(it, null)
          it.flush()
        } catch(e: IOException) {
          project.logger.warn("Failed to store persistent properties", e)
        }
      }
    }
  }
}
