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
public abstract class AbstractOptionsValidator extends AbstractOptionsExtension {

  protected AbstractOptionsValidator(Class<? extends Options<?>> declaringClass) {
    super(OptionExtensionType.VALIDATION, declaringClass);
  }

  protected abstract Set<ConstraintViolation<OptionsBuilder<?, ?>>> validate(
      OptionsBuilder<?, ?> options);

  @Override
  public void extend(OptionsBuilder<?, ?> options) {
    val validation = validate(options);
    if (!validation.isEmpty()) {
      // Could be improved by collecting validation from all validators in options factoring
      throw new ValidateOptionsException(validation, options);
    }
  }
}
