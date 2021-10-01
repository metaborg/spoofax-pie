package mb.codecompletion.bench

import dagger.Component
import mb.log.dagger.LoggerComponent
import mb.pie.dagger.PieComponent
import mb.resource.dagger.ResourceServiceComponent
import mb.spoofax.core.platform.PlatformComponent
import mb.spoofax.core.platform.PlatformScope

@PlatformScope
@Component(
  modules = [],
  dependencies = [
    LoggerComponent::class,
    ResourceServiceComponent::class,
    PieComponent::class
  ]
)
interface TestComponent: PlatformComponent {

}
