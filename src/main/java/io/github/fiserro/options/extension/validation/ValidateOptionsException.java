package io.github.fiserro.options.extension.validation;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.OptionsException;
import jakarta.validation.ConstraintViolation;
import java.util.Comparator;
import java.util.Set;
import lombok.Getter;

@Getter
public class ValidateOptionsException extends OptionsException {

  private final transient Set<ConstraintViolation<OptionsBuilder<?>>> validation;
  private final transient OptionsBuilder<?> options;

  public ValidateOptionsException(
      Set<ConstraintViolation<OptionsBuilder<?>>> validation,
      OptionsBuilder<? extends Options> options) {
    super(getMessage(validation));
    this.validation = validation;
    this.options = options;
  }

  private static String getMessage(Set<ConstraintViolation<OptionsBuilder<?>>> validation) {
    int size = validation.size();
    return size + " options validation failed:\n" + validation.stream()
        .sorted(Comparator.comparing(ConstraintViolation::getMessage))
        .map(ConstraintViolation::getMessage)
        .reduce((a, b) -> a + "\n" + b)
        .orElse("");
  }
}
