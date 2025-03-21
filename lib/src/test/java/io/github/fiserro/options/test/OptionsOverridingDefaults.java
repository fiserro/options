package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;

public interface OptionsOverridingDefaults extends OptionsIntegers, OptionsStrings {

  @Option
  @Override
  default Integer integerWithDefault() {
    return 20;
  }

  @Option
  @Override
  default String stringWithDefault() {
    return "different";
  }

  @Option
  default Integer integer() {
    return 25;
  }


}
