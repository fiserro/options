package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;

public interface StringsOptionsWith extends Strings, Options<StringsOptionsWith> {

  StringsOptionsWith withString(String string);

  @Option
  int integer();

  StringsOptionsWith withInteger(int integer);

}
