package io.github.fiserro.options.extension.validation;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.extension.AbstractOptionsExtension;
import io.github.fiserro.options.extension.OptionExtensionType;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import lombok.val;

/**
 * Abstract class for options validator.
 */
public abstract class AbstractOptionsValidator<T extends Options<T>, B extends OptionsBuilder<T, B>>
    extends AbstractOptionsExtension {

  protected AbstractOptionsValidator(Class<? extends Options<?>> declaringClass) {
    super(OptionExtensionType.VALIDATION, declaringClass);
  }

  public abstract Set<ConstraintViolation<T>> validate(
      OptionsBuilder<T, B> optionsBuilder, Options<T> options);

  @Override
  public void extend(OptionsBuilder<?, ?> options) {
    throw new UnsupportedOperationException();
  }
}
