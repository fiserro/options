package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.test.OptionsStrings;
import org.junit.jupiter.api.Test;

class NullValueTest {

  @Test
  void newInstanceIsCreatedWith() {
    OptionsStrings o1 = OptionsFactory.create(OptionsStrings.class);
    assertThat(o1.string(), is(nullValue()));
  }
}
