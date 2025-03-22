package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.extension.validation.jakarta.JakartaValidator.AbstractJakartaAnnotationValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import lombok.experimental.SuperBuilder;


@SuperBuilder
public class NotNullValidator extends AbstractJakartaAnnotationValidator<NotNull> {

  @Override
  public Optional<ConstraintViolation<OptionsBuilder<?>>> validate(
      OptionsBuilder<? extends Options> options,
      OptionDef option, Object value) {
    if (value != null) {
      return Optional.empty();
    }
    return Optional.of(
        new ConstraintViolationImpl(annotation.message(), value, option, options));
  }
}
