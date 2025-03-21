package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.fiserro.options.test.OptionsAll;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OptionsFactoryTest {

  @Test
  void optionsAreReadableFromGivenMap() {

    Map<String, Object> values = new HashMap<>();
    values.put("string", "localhost");
    values.put("listOfString", List.of("x", "y", "z"));

    OptionsAll options = OptionsFactory.create(OptionsAll.class, values);
    assertNotNull(options);
    assertThat(options.string(), is("localhost"));
    assertThat(options.listOfString(), is(List.of("x", "y", "z")));
  }

}