package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

import java.util.Map;
import org.junit.jupiter.api.Test;

class OptionsToStringTest {

  @Test
  void optionsHaveToStringMethodImplemented() {
    Map<String, Object> values = Map.of("name", "Mario");
    Options options = OptionsFactory.create(SimpleOptions.class, values);
    String toString = options.toString();
    assertThat(toString, matchesPattern("SimpleOptions@[0-9a-f]+\\{count=0, name=Mario}"));
  }

  public interface SimpleOptions extends Options {

    @Option
    String name();

    /**
     * @return 0 by default (primitive int)
     */
    @Option
    int count();

    @Option
    Integer notSet();
  }
}
