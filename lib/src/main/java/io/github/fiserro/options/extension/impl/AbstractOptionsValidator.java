package io.github.fiserro.options.extension.impl;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.OptionsException;
import io.github.fiserro.options.extension.OptionExtensionType;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.val;

/**
 * Abstract class for options validator.
 */
public abstract class AbstractOptionsValidator extends AbstractOptionsExtension {

  protected AbstractOptionsValidator(Class<? extends Options> declaringClass) {
    super(OptionExtensionType.VALIDATION, declaringClass);
  }

  protected abstract Validation<Seq<OptionsException>, Seq<OptionDef>> validate(
      OptionsBuilder<? extends Options> options);

  @Override
  public void extend(OptionsBuilder<? extends Options> options) {
    val validation = validate(options);
    if (validation.isInvalid()) {
      // Could be improved by collecting validation from all validators in options factoring
      throw new ValidateOptionsException(validation, options);
    }
  }

}
