package mb.ccbench.webdsl

import dagger.Component
import mb.ccbench.di.BenchLoggerComponent
import mb.ccbench.di.BenchPlatformComponent
import mb.ccbench.di.BenchResourceServiceComponent
import mb.ccbench.di.BenchScope
import mb.pie.dagger.RootPieComponent
import mb.webdsl.WebDSLResourcesComponent

@BenchScope
@Component(dependencies = [
    BenchLoggerComponent::class,
    WebDSLResourcesComponent::class,
    BenchResourceServiceComponent::class,
    BenchPlatformComponent::class,
    WebDSLBenchLanguageComponent::class,
    RootPieComponent::class,
])
interface WebDSLBenchComponent {
    val mainCommand: WebDSLMainCommand
}
