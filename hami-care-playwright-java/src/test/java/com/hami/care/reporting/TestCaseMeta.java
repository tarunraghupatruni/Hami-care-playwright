package com.hami.care.reporting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestCaseMeta {
    String id();
    String title();
    String preconditions() default "";
    String steps() default "";
    String expected() default "";
}
