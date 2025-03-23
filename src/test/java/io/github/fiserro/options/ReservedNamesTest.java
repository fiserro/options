package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsFactory;
import org.junit.jupiter.api.Test;

class ReservedNamesTest {

  @Test
  void failOnHashCode() {
    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> OptionsFactory.create(OptionsHashCode.class));
    assertThat(exception.getMessage(), matchesPattern("The option name hashCode is reserved"));
  }

  public interface OptionsHashCode extends Options {

    @Option
    int hashCode();
  }
}
