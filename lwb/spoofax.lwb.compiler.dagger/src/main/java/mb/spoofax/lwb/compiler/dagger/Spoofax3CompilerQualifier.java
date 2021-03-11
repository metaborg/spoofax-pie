package mb.spoofax.lwb.compiler.dagger;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Spoofax3CompilerQualifier {
    String value() default "";
}
