package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.extension.ArgumentsEquals;
import io.github.fiserro.options.extension.EnvironmentVariables;
import io.github.fiserro.options.extension.OptionsExtensions;
import io.github.fiserro.options.extension.validation.jakarta.JakartaValidator;
import jakarta.validation.constraints.NotNull;


@OptionsExtensions({ArgumentsEquals.class, EnvironmentVariables.class, JakartaValidator.class})
public interface NestedCompositionOptions extends Options<NestedCompositionOptions> {

  @NotNull
  @Option
  StringsOptions source();

  @NotNull
  @Option
  StringsOptions target();

}
