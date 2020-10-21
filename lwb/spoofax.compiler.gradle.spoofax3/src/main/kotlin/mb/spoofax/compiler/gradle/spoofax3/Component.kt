package mb.spoofax.compiler.gradle.spoofax3

import dagger.Component
import dagger.Module
import dagger.Provides
import mb.esv.EsvComponent
import mb.esv.EsvQualifier
import mb.libspoofax2.LibSpoofax2Component
import mb.libspoofax2.LibSpoofax2Qualifier
import mb.libstatix.LibStatixComponent
import mb.libstatix.LibStatixQualifier
import mb.pie.api.MapTaskDefs
import mb.pie.api.Pie
import mb.pie.api.TaskDef
import mb.resource.ResourceService
import mb.sdf3.Sdf3Component
import mb.sdf3.Sdf3Qualifier
import mb.spoofax.compiler.spoofax3.dagger.*
import mb.statix.StatixComponent
import mb.statix.StatixQualifier
import mb.str.StrategoComponent
import mb.str.StrategoQualifier

@Spoofax3CompilerScope
@Component(
  modules = [Spoofax3CompilerModule::class, Spoofax3CompilerGradleModule::class],
  dependencies = [
    Sdf3Component::class,
    StrategoComponent::class,
    EsvComponent::class,
    StatixComponent::class,
    LibSpoofax2Component::class,
    LibStatixComponent::class
  ]
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
    @StrategoQualifier strategoResourceService: ResourceService,
    @EsvQualifier esvResourceService: ResourceService,
    @StatixQualifier statixResourceService: ResourceService,
    @LibSpoofax2Qualifier libSpoofax2ResourceService: ResourceService,
    @LibStatixQualifier libStatixResourceService: ResourceService
  ): ResourceService {
    return parentResourceService.createChild(
      sdf3ResourceService,
      strategoResourceService,
      esvResourceService,
      statixResourceService,
      libSpoofax2ResourceService,
      libStatixResourceService
    )
  }

  @Provides
  @Spoofax3CompilerScope
  fun providePie(
    resourceService: ResourceService,
    taskDefs: MutableSet<TaskDef<*, *>>,
    @Sdf3Qualifier sdf3Pie: Pie,
    @StrategoQualifier strategoPie: Pie,
    @EsvQualifier esvPie: Pie,
    @StatixQualifier statixPie: Pie,
    @LibSpoofax2Qualifier libSpoofax2Pie: Pie,
    @LibStatixQualifier libStatixPie: Pie
  ): Pie {
    return parentPie.createChildBuilder(sdf3Pie, strategoPie, esvPie, statixPie, libSpoofax2Pie, libStatixPie)
      .withTaskDefs(MapTaskDefs(taskDefs))
      .withResourceService(resourceService)
      .build()
  }
}
