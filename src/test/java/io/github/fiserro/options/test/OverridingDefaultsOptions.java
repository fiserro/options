package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;

public interface OverridingDefaultsOptions extends Integers, Strings, Options<OverridingDefaultsOptions> {

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
