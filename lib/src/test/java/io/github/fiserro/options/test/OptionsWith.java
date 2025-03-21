package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;

public interface OptionsWith extends OptionsStrings {

  OptionsWith withString(String string);

  @Option
  int integer();

  OptionsWith withInteger(int integer);

}
