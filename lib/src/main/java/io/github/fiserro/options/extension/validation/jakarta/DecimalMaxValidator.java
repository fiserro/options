package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.extension.validation.jakarta.JakartaValidator.AbstractJakartaAnnotationValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class DecimalMaxValidator extends AbstractJakartaAnnotationValidator<DecimalMax> {

  @Override
  public Optional<ConstraintViolation<OptionsBuilder<?>>> validate(
      OptionsBuilder<? extends Options> options,
      OptionDef option, Object value) {
    return switch (value) {
      case null -> Optional.empty();
      case Number number -> validate(options, option, number);
      case String string -> validate(options, option, string);
      default -> Optional.empty();
    };
  }

  private Optional<ConstraintViolation<OptionsBuilder<?>>> validate(
      OptionsBuilder<? extends Options> options, OptionDef option, Number number) {
    return validate(options, option, number.toString());
  }

  private Optional<ConstraintViolation<OptionsBuilder<?>>> validate(
      OptionsBuilder<? extends Options> options, OptionDef option, String number) {
    return validate(options, option, new BigDecimal(number));
  }

  private Optional<ConstraintViolation<OptionsBuilder<?>>> validate(
      OptionsBuilder<? extends Options> options, OptionDef option, BigDecimal value) {
    BigDecimal maxValue = new BigDecimal(annotation.value());
    if (value.compareTo(maxValue) > 0) {
      return Optional.of(
          new ConstraintViolationImpl(annotation.message(), value, option, options));
    }
    return Optional.empty();
  }
}