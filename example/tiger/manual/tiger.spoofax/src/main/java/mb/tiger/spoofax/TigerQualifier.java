package mb.tiger.spoofax;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface TigerQualifier {
    String value() default "";
}
