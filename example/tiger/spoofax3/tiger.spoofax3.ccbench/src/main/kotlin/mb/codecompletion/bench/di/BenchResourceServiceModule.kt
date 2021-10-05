package mb.codecompletion.bench.di

import dagger.Module
import dagger.Provides
import mb.resource.text.TextResourceRegistry

@Module
class BenchResourceServiceModule(
    private val textResourceRegistry: TextResourceRegistry
) {
    @Provides
    fun provideTextResourceRegistry(): TextResourceRegistry {
        return textResourceRegistry
    }
}
