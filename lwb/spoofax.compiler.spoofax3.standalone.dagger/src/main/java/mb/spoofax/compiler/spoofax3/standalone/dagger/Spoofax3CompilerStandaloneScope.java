package mb.spoofax.compiler.spoofax3.standalone.dagger;

import javax.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface Spoofax3CompilerStandaloneScope {}
