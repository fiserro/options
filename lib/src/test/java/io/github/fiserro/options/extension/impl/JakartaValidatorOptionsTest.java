package io.github.fiserro.options.extension.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.extension.validation.ValidateOptionsException;
import io.github.fiserro.options.test.OptionsNotNull;
import org.junit.jupiter.api.Test;

class JakartaValidatorOptionsTest {


  // TODO then NotNull
  @Test
  void exceptionIsThrownWhenRequiredOptionIsNotSet() {
    Exception exception = assertThrows(ValidateOptionsException.class,
        () -> OptionsFactory.create(OptionsNotNull.class));
    assertThat(exception.getMessage(), matchesPattern("(?s)3 options validation failed:.*"));
  }
}
