package io.github.fiserro.options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this Annotation to define the option of the Options interface.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

  String description() default "";

//  boolean required() default false;

  String[] env() default {};

  Class<? extends ValueParser> parser() default ValueParserDefault.class;
}
