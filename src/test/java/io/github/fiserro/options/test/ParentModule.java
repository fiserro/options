package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Parent module with an option and wither.
 * Used to test that child interfaces can override the option while retaining access to the wither.
 */
public interface ParentModule<T extends ParentModule<T>> extends Options<T> {

  @Retention(RetentionPolicy.RUNTIME)
  @interface ParentAnnotation {
    String value() default "";
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface ChildAnnotation {
    String value() default "";
  }

  @Option
  @ParentAnnotation("from-parent")
  default int power() {
    return 50;
  }

  T withPower(int power);
}
