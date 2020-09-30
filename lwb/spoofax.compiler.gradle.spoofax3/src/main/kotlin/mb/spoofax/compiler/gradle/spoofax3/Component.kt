package mb.spoofax.compiler.gradle.spoofax3

import dagger.Component
import dagger.Module
import dagger.Provides
import mb.libspoofax2.LibSpoofax2ClassloaderResources
import mb.pie.api.MapTaskDefs
import mb.pie.api.Pie
import mb.pie.api.TaskDef
import mb.resource.ResourceService
import mb.sdf3.spoofax.Sdf3Component
import mb.sdf3.spoofax.Sdf3Qualifier
import mb.spoofax.compiler.spoofax3.dagger.*
import mb.str.spoofax.StrategoComponent
import mb.str.spoofax.StrategoQualifier

@Spoofax3CompilerScope
@Component(
  modules = [Spoofax3CompilerModule::class, Spoofax3CompilerGradleModule::class],
  dependencies = [Sdf3Component::class, StrategoComponent::class]
)
interface Spoofax3CompilerGradleComponent : Spoofax3CompilerComponent {
  val resourceService: ResourceService
  val pie: Pie
}

@Module
class Spoofax3CompilerGradleModule(
  val parentResourceService: ResourceService,
  val parentPie: Pie
) {
  @Provides
  @Spoofax3CompilerScope
  fun provideResourceService(
    @Sdf3Qualifier sdf3ResourceService: ResourceService,
    @StrategoQualifier strategoResourceService: ResourceService
  ): ResourceService {
    val registries = listOf(LibSpoofax2ClassloaderResources.createClassLoaderResourceRegistry())
    val additionalAncestors = listOf(sdf3ResourceService, strategoResourceService)
    return parentResourceService.createChild(registries, additionalAncestors)
  }

  @Provides
  @Spoofax3CompilerScope
  fun providePie(
    resourceService: ResourceService,
    taskDefs: MutableSet<TaskDef<*, *>>,
    @Sdf3Qualifier sdf3Pie: Pie,
    @StrategoQualifier strategoPie: Pie
  ): Pie {
    return parentPie.createChildBuilder(sdf3Pie, strategoPie)
      .withTaskDefs(MapTaskDefs(taskDefs))
      .withResourceService(resourceService)
      .build()
  }
}
