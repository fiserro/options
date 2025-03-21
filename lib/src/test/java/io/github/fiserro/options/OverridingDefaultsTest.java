package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.test.OptionsOverridingDefaults;
import org.junit.jupiter.api.Test;

class OverridingDefaultsTest {

  @Test
  void optionsReturnOverriddenDefaultValues() {
    OptionsOverridingDefaults options = OptionsFactory.create(OptionsOverridingDefaults.class);
    assertThat(options.integer(), is(25));
    assertThat(options.integerWithDefault(), is(20));
    assertThat(options.stringWithDefault(), is("different"));
  }
}
