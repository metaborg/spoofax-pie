package mb.codecompletion.bench.di

import dagger.Module
import dagger.Provides
import mb.log.api.LoggerFactory
import mb.log.dagger.LoggerScope
import mb.log.slf4j.SLF4JLoggerFactory

@Module
class BenchLoggerModule {
  @Provides @LoggerScope fun provideLoggerFactory(): LoggerFactory = SLF4JLoggerFactory()
}
