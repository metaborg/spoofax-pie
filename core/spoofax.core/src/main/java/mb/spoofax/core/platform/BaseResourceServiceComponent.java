package mb.spoofax.core.platform;

import dagger.Component;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.url.URLResourceRegistry;

@ResourceServiceScope
@Component(modules = {
    ResourceServiceProviderModule.class,
    BaseResourceServiceModule.class
})
public interface BaseResourceServiceComponent extends ResourceServiceComponent {
    FSResourceRegistry getFsResourceRegistry();

    URLResourceRegistry getUrlResourceRegistry();
}
