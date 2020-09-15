package mb.spoofax.compiler.gradle.spoofax2

import dagger.Component
import dagger.Module
import dagger.Provides
import mb.pie.api.MapTaskDefs
import mb.pie.api.Pie
import mb.pie.api.TaskDef
import mb.resource.ResourceService
import mb.spoofax.compiler.spoofax2.dagger.*
import javax.inject.Singleton

@Singleton
@Component(modules = [Spoofax2CompilerModule::class, Spoofax2CompilerGradleModule::class])
interface Spoofax2CompilerGradleComponent : Spoofax2CompilerComponent {
  val resourceService: ResourceService
  val pie: Pie
}

@Module
class Spoofax2CompilerGradleModule(
  val parentResourceService: ResourceService,
  val parentPie: Pie
) {
  @Provides
  @Singleton
  fun provideResourceService(): ResourceService {
    return parentResourceService // No need to create a child, as there are no changes.
  }

  @Provides
  @Singleton
  fun providePie(resourceService: ResourceService, taskDefs: MutableSet<TaskDef<*, *>>): Pie {
    return parentPie.createChildBuilder()
      .withTaskDefs(MapTaskDefs(taskDefs))
      .withResourceService(resourceService)
      .build()
  }
}
