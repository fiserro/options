package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import io.github.fiserro.options.test.AllOptions;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OptionsEqualsTest {

  @Test
  void optionsAreEqual() {
    AllOptions options1 = OptionsFactory.create(AllOptions.class,
        Map.of("primitiveInt", 1, "string", "test"));
    AllOptions options2 = OptionsFactory.create(AllOptions.class, "--primitiveInt=1",
        "--string=test");
    assertThat(options1, is(options2));
  }

  @Test
  void differentOptionsAreNotEqual() {
    AllOptions options1 = OptionsFactory.create(AllOptions.class,
        Map.of("primitiveInt", 1, "string", "test"));
    AllOptions options2 = OptionsFactory.create(AllOptions.class, "--primitiveInt=2", "--string=x");
    assertThat(options1, not(options2));
  }

  @Test
  void changedOptionsAreNotEqual() {
    AllOptions options1 = OptionsFactory.create(AllOptions.class,
        Map.of("primitiveInt", 1, "string", "test"));
    AllOptions options2 = (AllOptions) options1.withValue("primitiveInt", 2);
    assertThat(options1, not(options2));
  }

}
