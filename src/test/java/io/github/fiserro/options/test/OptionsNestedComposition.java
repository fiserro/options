package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.extension.ArgumentsEquals;
import io.github.fiserro.options.extension.EnvironmentVariables;
import io.github.fiserro.options.extension.OptionsExtensions;


@OptionsExtensions({ArgumentsEquals.class, EnvironmentVariables.class})
public interface OptionsNestedComposition extends Options<OptionsNestedComposition> {

  @Option
  StringsOptions source();

  @Option
  StringsOptions target();

}
