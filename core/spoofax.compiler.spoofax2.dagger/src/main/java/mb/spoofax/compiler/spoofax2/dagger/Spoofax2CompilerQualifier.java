package mb.spoofax.compiler.spoofax2.dagger;

import javax.inject.Qualifier;
import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Spoofax2CompilerQualifier {

}
