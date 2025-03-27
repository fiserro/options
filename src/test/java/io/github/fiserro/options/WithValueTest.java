package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.test.StringsOptionsWith;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WithValueTest {

  @Test
  void newInstanceIsCreatedWith() {
    StringsOptionsWith o1 = OptionsFactory.create(StringsOptionsWith.class, Map.of("string", "text"));
    StringsOptionsWith o2 = o1
        .withString("different text")
        .withInteger(10);

    assertThat(o1.string(), is("text"));
    assertThat(o2.string(), is("different text"));
    assertThat(o1.integer(), is(0));
    assertThat(o2.integer(), is(10));
  }
}
