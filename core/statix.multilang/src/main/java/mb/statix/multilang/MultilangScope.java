package mb.statix.multilang;

import javax.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Scope that should be effectively singleton.
 * Is needed to work around the restriction that @Singleton components can not depend on other @Singleton components
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface MultilangScope {
}
