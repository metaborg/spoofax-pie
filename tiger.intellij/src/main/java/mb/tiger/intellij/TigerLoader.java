package mb.tiger.intellij;

import com.intellij.ide.ApplicationLoadListener;
import com.intellij.openapi.application.Application;

public class TigerLoader implements ApplicationLoadListener {
    @Override public void beforeApplicationLoaded(Application application, String configPath) {
        try {
            TigerPlugin.init();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initializer Tiger plugin", e);
        }
    }
}
