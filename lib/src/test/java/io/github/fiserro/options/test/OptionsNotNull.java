package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.extension.OptionsExtensions;
import io.github.fiserro.options.extension.validation.jakarta.JakartaValidator;
import jakarta.validation.constraints.NotNull;

@OptionsExtensions({JakartaValidator.class})
public interface OptionsNotNull extends Options {

  @NotNull
  @Option
  String requiredString();

  @NotNull
  @Option
  Integer requiredInteger();

  @NotNull
  @Option
  Boolean requiredBoolean();
}
