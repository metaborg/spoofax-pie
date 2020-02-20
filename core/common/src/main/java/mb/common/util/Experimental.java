package mb.common.util;

import java.lang.annotation.*;

/**
 * This element is experimental.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
    ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.PACKAGE,
    ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER
})
public @interface Experimental {}
