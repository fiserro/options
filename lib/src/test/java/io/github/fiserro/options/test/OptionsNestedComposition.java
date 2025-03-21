package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.extension.OptionsExtensions;
import io.github.fiserro.options.extension.impl.ArgumentsEquals;
import io.github.fiserro.options.extension.impl.EnvironmentVariables;


@OptionsExtensions({ArgumentsEquals.class, EnvironmentVariables.class})
public interface OptionsNestedComposition extends Options {

  @Option
  OptionsStrings source();

  @Option
  OptionsStrings target();

}
