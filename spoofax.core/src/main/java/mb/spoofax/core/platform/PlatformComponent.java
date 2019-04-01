package mb.spoofax.core.platform;

import dagger.Component;

import javax.inject.Singleton;

@Singleton @Component(modules = FileSystemResourceServiceModule.class)
public interface PlatformComponent {
    ResourceService resourceService();
}
