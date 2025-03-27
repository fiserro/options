package io.github.fiserro.options.extension.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.extension.ArgumentsSpace;
import io.github.fiserro.options.extension.OptionsExtensions;
import io.github.fiserro.options.test.DateTime;
import io.github.fiserro.options.test.DependentDefaults;
import io.github.fiserro.options.test.Enum;
import io.github.fiserro.options.test.Integers;
import io.github.fiserro.options.test.Longs;
import io.github.fiserro.options.test.AllOptions;
import io.github.fiserro.options.test.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LoadArgumentsTest {

  @Test
  void optionsAreFilledFromEqualsSeparatedArguments() {
    AllOptions options = OptionsFactory.create(AllOptions.class, "--localDate=2024-01-01",
        "--enumValue=ONE", "--primitiveInt=1",
        "--primitiveLong=1", "--string=text");
    assertThat(options.localDate(), is(LocalDate.of(2024, 1, 1)));
    assertThat(options.enumValue(), is(AllOptions.TestEnum.ONE));
    assertThat(options.primitiveInt(), is(1));
    assertThat(options.primitiveLong(), is(1L));
    assertThat(options.string(), is("text"));
  }

  @Test
  void optionsAreFilledFromSpaceSeparatedArguments() {
    OptionsSpace options = OptionsFactory.create(OptionsSpace.class, "--localDate", "2024-01-01",
        "--enumValue", "ONE",
        "--primitiveInt", "1", "--primitiveLong", "1", "--string", "text");
    assertThat(options.localDate(), is(LocalDate.of(2024, 1, 1)));
    assertThat(options.enumValue(), is(AllOptions.TestEnum.ONE));
    assertThat(options.primitiveInt(), is(1));
    assertThat(options.primitiveLong(), is(1L));
    assertThat(options.string(), is("text"));
  }

  @OptionsExtensions(ArgumentsSpace.class)
  public interface OptionsSpace extends DateTime, DependentDefaults, Enum, Integers, Longs, Strings,
      Options<OptionsSpace> {

  }
}