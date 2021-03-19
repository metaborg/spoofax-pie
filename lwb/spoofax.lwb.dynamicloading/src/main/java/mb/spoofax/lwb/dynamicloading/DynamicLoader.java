package mb.spoofax.lwb.dynamicloading;

import mb.pie.api.MixedSession;
import mb.pie.api.Pie;

import javax.inject.Inject;
import java.io.IOException;

public class DynamicLoader implements AutoCloseable {
    private final DynamicLoad dynamicLoad;

    @Inject public DynamicLoader(DynamicLoad dynamicLoad) {
        this.dynamicLoad = dynamicLoad;
    }

    @Override public void close() throws IOException {
        dynamicLoad.close();
    }

    /**
     * Creates a new session for dynamically (re)loading languages, mimicking {@link MixedSession PIE's MixedSession}.
     *
     * @return Session for dynamically (re)loading languages. Must be closed after use with {@link
     * DynamicLoaderMixedSession#close}.
     */
    public DynamicLoaderMixedSession newSession(Pie pie) {
        return new DynamicLoaderMixedSession(pie.newSession(), dynamicLoad);
    }
}
