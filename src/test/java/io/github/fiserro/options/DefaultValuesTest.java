package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsFactory;
import org.junit.jupiter.api.Test;

class DefaultValuesTest {

  public interface OptionsDefaultValues extends Options {

    int DEFAULT_VALUE = 42;

    @Option
    default int value() {
      return DEFAULT_VALUE;
    }
  }

  @Test
  void declaredValueOverridesPrimitiveDefault() {
    OptionsDefaultValues options = OptionsFactory.create(OptionsDefaultValues.class);
    assertThat(options.value(), is(OptionsDefaultValues.DEFAULT_VALUE));
  }
}
