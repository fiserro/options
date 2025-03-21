package io.github.fiserro.options.extension.validation;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.OptionsException;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import lombok.Getter;

@Getter
public class ValidateOptionsException extends OptionsException {

  private final transient Set<ConstraintViolation<OptionDef>> validation;
  private final transient OptionsBuilder<? extends Options> options;

  public ValidateOptionsException(
      Set<ConstraintViolation<OptionDef>> validation,
      OptionsBuilder<? extends Options> options) {
    super(getMessage(validation));
    this.validation = validation;
    this.options = options;
  }

  private static String getMessage(Set<ConstraintViolation<OptionDef>> validation) {
    int size = validation.size();
    return size + " options validation failed: " + validation.stream()
        .map(ConstraintViolation::getMessage)
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }
}
