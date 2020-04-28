package mb.spoofax.core.platform;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A dependency-injection {@link Qualifier qualifier annotation} for instances of {@link PlatformComponent platform
 * components}.
 */
@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Platform {
}
