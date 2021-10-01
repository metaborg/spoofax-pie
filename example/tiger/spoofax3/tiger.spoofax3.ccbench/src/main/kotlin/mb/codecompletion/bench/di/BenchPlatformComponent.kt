package mb.codecompletion.bench.di

import dagger.Component
import mb.spoofax.core.platform.PlatformComponent
import mb.spoofax.core.platform.PlatformScope


@PlatformScope
@Component(dependencies = [
  BenchLoggerComponent::class,
  BenchResourceServiceComponent::class
])
interface BenchPlatformComponent: PlatformComponent {

}
