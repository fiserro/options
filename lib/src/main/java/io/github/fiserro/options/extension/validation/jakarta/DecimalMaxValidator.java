package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.extension.validation.jakarta.JakartaValidator.AbstractJakartaAnnotationValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class DecimalMaxValidator extends AbstractJakartaAnnotationValidator<DecimalMax> {

  @Override
  public Optional<ConstraintViolation<OptionDef>> validate(OptionDef option, Object value) {
    return switch (value) {
      case null -> Optional.empty();
      case Number number -> validate(option, number);
      case String string -> validate(option, string);
      default -> Optional.empty();
    };
  }

  private Optional<ConstraintViolation<OptionDef>> validate(OptionDef option, Number number) {
    return validate(option, number.toString());
  }

  private Optional<ConstraintViolation<OptionDef>> validate(OptionDef option, String number) {
    return validate(option, new BigDecimal(number));
  }

  private Optional<ConstraintViolation<OptionDef>> validate(OptionDef option, BigDecimal value) {
    BigDecimal maxValue = new BigDecimal(annotation.value());
    if (value.compareTo(maxValue) > 0) {
      return Optional.of(
          new ConstraintViolationImpl<>(option, annotation.message(), value, option, null));
    }
    return Optional.empty();
  }
}