package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.extension.validation.jakarta.JakartaValidator.AbstractJakartaAnnotationValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import lombok.experimental.SuperBuilder;


@SuperBuilder
public class NotNullValidator extends AbstractJakartaAnnotationValidator<NotNull> {

  @Override
  public Optional<ConstraintViolation<OptionDef>> validate(OptionDef option, Object value) {
    if (value != null) {
      return Optional.empty();
    }
    return Optional.of(
        new ConstraintViolationImpl<>(option, annotation.message(), value, option, null));
  }
}
