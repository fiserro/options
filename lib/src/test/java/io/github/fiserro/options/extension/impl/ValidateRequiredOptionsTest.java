package io.github.fiserro.options.extension.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.test.OptionsRequired;
import org.junit.jupiter.api.Test;

class ValidateRequiredOptionsTest {

  @Test
  void exceptionIsThrownWhenRequiredOptionIsNotSet() {
    Exception exception = assertThrows(ValidateOptionsException.class,
        () -> OptionsFactory.create(OptionsRequired.class));
    assertThat(exception.getMessage(), matchesPattern("(?s)3 options validation failed:.*"));
  }
}
