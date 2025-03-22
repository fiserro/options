package io.github.fiserro.options.extension.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.extension.validation.ValidateOptionsException;
import io.github.fiserro.options.test.JakartaValidatedTestOptions;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JakartaValidatorOptionsTest {

  @Test
  void exceptionIsThrownWhenRequiredOptionIsNotSet() {

    Map<String, Object> values = Map.of(
        "list", List.of()
    );

    Exception exception = assertThrows(ValidateOptionsException.class,
        () -> OptionsFactory.create(JakartaValidatedTestOptions.class, values));
    assertThat(exception.getMessage(), matchesPattern("(?s)4 options validation failed:.*"));
  }
}
