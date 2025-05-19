package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.extension.EnvironmentVariables;
import io.github.fiserro.options.extension.OptionsExtensions;

@OptionsExtensions(EnvironmentVariables.class)
public interface StringsOptionsWith extends Strings, Options<StringsOptionsWith> {

  StringsOptionsWith withString(String string);

  @Option
  int integer();

  StringsOptionsWith withInteger(int integer);

}
