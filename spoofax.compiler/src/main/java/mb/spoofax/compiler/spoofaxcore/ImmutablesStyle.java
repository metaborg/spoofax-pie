package mb.spoofax.compiler.spoofaxcore;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS) // Class retention for incremental compilation.
@Value.Style(
    typeImmutableEnclosing = "*Data",
    deepImmutablesDetection = true,
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true
)
public @interface ImmutablesStyle {}
