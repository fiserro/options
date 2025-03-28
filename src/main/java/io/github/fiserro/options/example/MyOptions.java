package io.github.fiserro.options.example;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.example.JdbcProperties.JdbcOptions;
import io.github.fiserro.options.extension.ArgumentsEquals;
import io.github.fiserro.options.extension.EnvironmentVariables;
import io.github.fiserro.options.extension.OptionsExtensions;

@OptionsExtensions({EnvironmentVariables.class, ArgumentsEquals.class})
public interface MyOptions extends Options<MyOptions> {

  @Option
  JdbcOptions primarydb();

  @Option
  JdbcOptions secondarydb();

}
