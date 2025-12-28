package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.extension.Envio;
import io.github.fiserro.options.test.DefaultWithWither;
import io.github.fiserro.options.test.StringsOptionsWith;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WithValueTest {

  @Test
  void witherWorksWithDefaultMethod() {
    DefaultWithWither state = OptionsFactory.create(DefaultWithWither.class);
    assertThat("default value", state.value(), is(50));

    DefaultWithWither modified = state.withValue(30);
    assertThat("modified value", modified.value(), is(30));
  }

  @Test
  void programArgumentsHasLowerPriorityThanWithValue() {
    StringsOptionsWith o1 = OptionsFactory.create(StringsOptionsWith.class, Map.of("string", "text"));
    StringsOptionsWith o2 = o1
        .withString("different text")
        .withInteger(10);

    assertThat(o1.string(), is("text"));
    assertThat(o2.string(), is("different text"));
    assertThat(o1.integer(), is(0));
    assertThat(o2.integer(), is(10));
  }

  @Test
  void environmentVariablesHasLowerPriorityThanWithValue() {
    Envio.setVar("STRING", "text from env");
    Envio.setVar("INTEGER", "0");
    StringsOptionsWith o1 = OptionsFactory.create(StringsOptionsWith.class);
    StringsOptionsWith o2 = o1
        .withString("different text")
        .withInteger(10);

    assertThat(o1.string(), is("text from env"));
    assertThat(o2.string(), is("different text"));
    assertThat(o1.integer(), is(0));
    assertThat(o2.integer(), is(10));

    Envio.clear();
  }
}
