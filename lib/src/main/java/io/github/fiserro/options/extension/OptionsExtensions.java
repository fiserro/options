package io.github.fiserro.options.extension;

import io.github.fiserro.options.extension.impl.ArgumentsEquals;
import io.github.fiserro.options.extension.impl.ArgumentsSpace;
import io.github.fiserro.options.extension.impl.EnvironmentVariables;
import io.github.fiserro.options.extension.impl.ValidateRequired;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this Annotation to extend the Options, for example to set the value of the option from given
 * program arguments or environment variables. You can provide multiple extensions.<p>
 * <p>
 * Exclusive extensions cannot be combined with others of the same type.<p>
 * <p>
 * For instance, if you use {@link ArgumentsSpace} and {@link ArgumentsEquals} on the same class, it
 * will throw an exception because both are of the same type.<p>
 * <p>
 * When Options interface uses {@link ArgumentsSpace} and extended interface uses
 * {@link ArgumentsEquals}, the {@link ArgumentsSpace} will be replaced with
 * {@link ArgumentsEquals}.<p>
 * </p>
 *
 * @see OptionsExtension
 * @see ArgumentsEquals
 * @see ArgumentsSpace
 * @see EnvironmentVariables
 * @see ValidateRequired
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionsExtensions {

  Class<? extends OptionsExtension>[] value() default {};
}
