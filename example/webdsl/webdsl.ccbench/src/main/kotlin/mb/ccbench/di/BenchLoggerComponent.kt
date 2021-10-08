package mb.ccbench.di

import dagger.Component
import mb.log.dagger.LoggerComponent
import mb.log.dagger.LoggerScope

@LoggerScope
@Component(modules = [
  BenchLoggerModule::class
])
interface BenchLoggerComponent: LoggerComponent {
}
