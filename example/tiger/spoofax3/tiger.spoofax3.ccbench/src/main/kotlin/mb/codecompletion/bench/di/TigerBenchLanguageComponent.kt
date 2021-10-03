package mb.codecompletion.bench.di

import dagger.Component
import mb.codecompletion.bench.CompletenessTest
import mb.codecompletion.bench.PrepareTestFileTask
import mb.resource.dagger.ResourceServiceComponent
import mb.tiger.TigerComponent
import mb.tiger.TigerModule
import mb.tiger.TigerResourcesComponent
import mb.tiger.TigerScope

@TigerScope
@Component(
  modules = [
    TigerModule::class,
    TigerBenchModule::class
  ],
  dependencies = [
    BenchLoggerComponent::class,
    TigerResourcesComponent::class,
    ResourceServiceComponent::class,
    BenchPlatformComponent::class
  ]
)
interface TigerBenchLanguageComponent: BenchLanguageComponent, TigerComponent {
//  val testTask: CompletenessTest.TestTask
  val prepareTestFileTask: PrepareTestFileTask
}
