package mb.spoofax.compiler.spoofax3.standalone.dagger;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Spoofax3CompilerStandaloneQualifier {
    String value() default "";
}
