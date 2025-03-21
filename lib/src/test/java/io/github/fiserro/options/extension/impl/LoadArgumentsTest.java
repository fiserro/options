package io.github.fiserro.options.extension.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.extension.ArgumentsSpace;
import io.github.fiserro.options.extension.OptionsExtensions;
import io.github.fiserro.options.test.OptionsAll;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LoadArgumentsTest {

  @Test
  void optionsAreFilledFromEqualsSeparatedArguments() {
    OptionsAll options = OptionsFactory.create(OptionsAll.class, "--localDate=2024-01-01",
        "--enumValue=ONE", "--primitiveInt=1",
        "--primitiveLong=1", "--string=text");
    assertThat(options.localDate(), is(LocalDate.of(2024, 1, 1)));
    assertThat(options.enumValue(), is(OptionsAll.TestEnum.ONE));
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
    assertThat(options.enumValue(), is(OptionsAll.TestEnum.ONE));
    assertThat(options.primitiveInt(), is(1));
    assertThat(options.primitiveLong(), is(1L));
    assertThat(options.string(), is("text"));
  }

  @OptionsExtensions(ArgumentsSpace.class)
  public interface OptionsSpace extends OptionsAll {

  }
}