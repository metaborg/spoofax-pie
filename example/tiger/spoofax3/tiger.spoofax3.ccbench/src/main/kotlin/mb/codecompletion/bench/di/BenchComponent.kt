package mb.codecompletion.bench.di

import dagger.Component
import mb.codecompletion.bench.MainCommand
import mb.pie.dagger.RootPieComponent
import mb.tiger.TigerResourcesComponent

@BenchScope
@Component(dependencies = [
    BenchLoggerComponent::class,
    TigerResourcesComponent::class,
    BenchResourceServiceComponent::class,
    BenchPlatformComponent::class,
    TigerBenchLanguageComponent::class,
    RootPieComponent::class
])
interface BenchComponent {
    val mainCommand: MainCommand
}
