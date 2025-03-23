package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.test.OptionsAll;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OptionsEqualsTest {

  @Test
  void optionsAreEqual() {
    OptionsAll options1 = OptionsFactory.create(OptionsAll.class,
        Map.of("primitiveInt", 1, "string", "test"));
    OptionsAll options2 = OptionsFactory.create(OptionsAll.class, "--primitiveInt=1",
        "--string=test");
    assertThat(options1, is(options2));
  }

  @Test
  void differentOptionsAreNotEqual() {
    OptionsAll options1 = OptionsFactory.create(OptionsAll.class,
        Map.of("primitiveInt", 1, "string", "test"));
    OptionsAll options2 = OptionsFactory.create(OptionsAll.class, "--primitiveInt=2", "--string=x");
    assertThat(options1, not(options2));
  }

  @Test
  void changedOptionsAreNotEqual() {
    OptionsAll options1 = OptionsFactory.create(OptionsAll.class,
        Map.of("primitiveInt", 1, "string", "test"));
    OptionsAll options2 = options1.withValue("primitiveInt", 2);
    assertThat(options1, not(options2));
  }

}
