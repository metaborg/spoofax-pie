package mb.spoofax.compiler.gradle.spoofaxcore

import dagger.Component
import dagger.Module
import dagger.Provides
import mb.pie.api.MapTaskDefs
import mb.pie.api.Pie
import mb.pie.api.PieBuilder
import mb.pie.api.TaskDef
import mb.resource.DefaultResourceService
import mb.resource.ResourceService
import mb.resource.fs.FSResourceRegistry
import mb.spoofax.compiler.dagger.*
import mb.spoofax.compiler.spoofax2.dagger.*
import javax.inject.Singleton

@Singleton
@Component(modules = [SpoofaxCompilerModule::class, Spoofax2CompilerModule::class, SpoofaxCompilerGradleModule::class])
interface SpoofaxCompilerGradleComponent : Spoofax2CompilerComponent {
  val resourceService: ResourceService

  val pie: Pie
}

@Module
class SpoofaxCompilerGradleModule(
  val builderSupplier: () -> PieBuilder
) {
  @Provides
  @Singleton
  fun provideResourceService(): ResourceService {
    return DefaultResourceService(FSResourceRegistry())
  }

  @Provides
  @Singleton
  fun providePie(resourceService: ResourceService, taskDefs: MutableSet<TaskDef<*, *>>): Pie {
    val builder: PieBuilder = builderSupplier()
    builder.withTaskDefs(MapTaskDefs(taskDefs))
    builder.withResourceService(resourceService)
    return builder.build()
  }
}
