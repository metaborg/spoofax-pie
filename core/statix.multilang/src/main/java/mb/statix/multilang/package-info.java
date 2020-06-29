// @formatter:off
@Value.Style(
    get = { "is*", "get*" },
    // prevent generation of javax.annotation.*; bogus entry, because empty list = allow all
    allowedClasspathAnnotations = {Override.class}
)
@DefaultQualifier(NonNull.class)
package mb.statix.multilang;
// @formatter:on

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.immutables.value.Value;
