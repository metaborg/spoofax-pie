// @formatter:off
@Value.Style(
    get = { "is*", "get*" },
    // prevent generation of javax.annotation.*; bogus entry, because empty list = allow all
    allowedClasspathAnnotations = {Override.class}
)
package mb.statix.multilang;
// @formatter:on

import org.immutables.value.Value;
