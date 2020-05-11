package mb.common.util;

import org.derive4j.Data;
import org.derive4j.Derive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.derive4j.Make.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Data(@Derive(make = {lambdaVisitor, constructors, getters, casesMatching, caseOfMatching}))
public @interface ADT {}
