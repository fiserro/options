package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.test.OverridingDefaultsOptions;
import org.junit.jupiter.api.Test;

class OverridingDefaultsTest {

  @Test
  void optionsReturnOverriddenDefaultValues() {
    OverridingDefaultsOptions options = OptionsFactory.create(OverridingDefaultsOptions.class);
    assertThat(options.integer(), is(25));
    assertThat(options.integerWithDefault(), is(20));
    assertThat(options.stringWithDefault(), is("different"));
  }
}
