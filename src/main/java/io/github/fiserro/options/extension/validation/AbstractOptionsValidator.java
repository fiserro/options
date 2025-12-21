package io.github.fiserro.options.extension.validation;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.extension.AbstractOptionsExtension;
import io.github.fiserro.options.extension.OptionExtensionType;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

/**
 * Abstract class for options validator.
 */
public abstract class AbstractOptionsValidator<T extends Options<T>>
    extends AbstractOptionsExtension {

  protected AbstractOptionsValidator() {
    super(OptionExtensionType.VALIDATION);
  }

  public abstract Set<ConstraintViolation<T>> validate(Options<T> options,
      OptionsBuilder<?, ?> builder);

  @Override
  public final void extend(OptionsBuilder<? extends Options<?>, ?> options) {
    throw new UnsupportedOperationException();
  }
}
