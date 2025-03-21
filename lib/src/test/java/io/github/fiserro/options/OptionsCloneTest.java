package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.test.OptionsAll;
import org.junit.jupiter.api.Test;

class OptionsCloneTest {

  @Test
  void optionsAreCloned() {
    OptionsAll options1 = OptionsFactory.create(OptionsAll.class, "--primitiveInt=1",
        "--string=test");
    OptionsAll options2 = OptionsFactory.clone(options1);
    assertThat(options1, is(options2));
  }

}
