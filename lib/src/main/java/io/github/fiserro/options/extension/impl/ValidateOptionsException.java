package io.github.fiserro.options.extension.impl;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.OptionsException;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.Getter;

@Getter
public class ValidateOptionsException extends OptionsException {

  private final transient Validation<Seq<OptionsException>, Seq<OptionDef>> validation;
  private final transient OptionsBuilder<? extends Options> options;

  public ValidateOptionsException(Validation<Seq<OptionsException>, Seq<OptionDef>> validation,
      OptionsBuilder<? extends Options> options) {
    super(getMessage(validation));
    this.validation = validation;
    this.options = options;
  }

  private static String getMessage(Validation<Seq<OptionsException>, Seq<OptionDef>> validation) {
    int size = validation.isValid() ? validation.getOrElse(List.empty()).size()
        : validation.getError().size();
    return validation.fold(
        errors -> size + " options validation failed: " + errors.mkString(", "),
        users -> size + " options passed the validation"
    );
  }
}
