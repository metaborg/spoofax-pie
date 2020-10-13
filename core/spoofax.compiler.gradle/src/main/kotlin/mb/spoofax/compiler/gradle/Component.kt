package mb.spoofax.compiler.gradle

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
import org.gradle.api.logging.Logger

@SpoofaxCompilerScope
@Component(modules = [SpoofaxCompilerModule::class, SpoofaxCompilerGradleModule::class])
interface SpoofaxCompilerGradleComponent : SpoofaxCompilerComponent {
  val resourceService: ResourceService
  val pie: Pie
}

@Module
class SpoofaxCompilerGradleModule(
  private val logger: Logger,
  private val builderSupplier: () -> PieBuilder
) {
  @Provides
  @SpoofaxCompilerScope
  fun provideResourceService(): ResourceService {
    return DefaultResourceService(FSResourceRegistry())
  }

  @Provides
  @SpoofaxCompilerScope
  fun providePie(resourceService: ResourceService, taskDefs: MutableSet<TaskDef<*, *>>): Pie {
    val builder: PieBuilder = builderSupplier()
    builder.withTaskDefs(MapTaskDefs(taskDefs))
    builder.withResourceService(resourceService)
    builder.withLogger(PieLogger(logger))
    return builder.build()
  }
}
