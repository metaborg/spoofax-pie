package mb.spoofax.core.component;

import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.log.dagger.LoggerComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.stream.Stream;

public interface ComponentManager extends AutoCloseable {
    LoggerComponent getLoggerComponent();

    PlatformComponent getPlatformComponent();


    @Nullable Component getComponent(Coordinate coordinate);

    @Nullable LanguageComponent getLanguageComponent(Coordinate coordinate);
}
