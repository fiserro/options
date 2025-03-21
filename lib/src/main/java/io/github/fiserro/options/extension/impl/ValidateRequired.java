package io.github.fiserro.options.extension.impl;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.OptionsException;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import java.util.Comparator;
import java.util.List;

/**
 * Extension that validates if all required options are set.
 */
public class ValidateRequired extends AbstractOptionsValidator {

  public ValidateRequired(Class<? extends Options> declaringClass) {
    super(declaringClass);
  }

  @Override
  protected Validation<Seq<OptionsException>, Seq<OptionDef>> validate(
      OptionsBuilder<? extends Options> options) {
    List<OptionDef> optionsList = options.options().values().stream()
        .distinct()
        .sorted(Comparator.comparing(OptionDef::name))
        .toList();
    return Validation.traverse(optionsList, option -> validate(options, option));
  }

  private Validation<Seq<OptionsException>, OptionDef> validate(
      OptionsBuilder<? extends Options> options,
      OptionDef option) {
    if (!option.hasDefaultValue() && option.required() && getValue(options, option) == null) {
      return Validation.invalid(io.vavr.collection.List.of(
          new OptionsException("Option '" + option.name() + "' is required")));
    }
    return Validation.valid(option);
  }

  private Object getValue(OptionsBuilder<? extends Options> options, OptionDef option) {
    try {
      return options.getValueOrPrimitiveDefault(option.name());
    } catch (Exception ignored) {
      // getting a value may fail if some dependencies are not set. in this case we consider the option as not set
      return null;
    }
  }
}
