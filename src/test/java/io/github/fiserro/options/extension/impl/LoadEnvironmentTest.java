package io.github.fiserro.options.extension.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.extension.Envio;
import io.github.fiserro.options.test.AllOptions;
import org.junit.jupiter.api.Test;

class LoadEnvironmentTest {

  @Test
  void optionsAreFilledWithEnvironmentVariables() {

    Envio.setVar("ENUM_VALUE", "TWO");
    Envio.setVar("PRIMITIVE_INT", "5");
    Envio.setVar("STRING", "test");

    AllOptions options = OptionsFactory.create(AllOptions.class);
    assertThat(options.enumValue(), is(AllOptions.TestEnum.TWO));
    assertThat(options.primitiveInt(), is(5));
    assertThat(options.string(), is("test"));

    Envio.resetVar("ENUM_VALUE");
    Envio.resetVar("PRIMITIVE_INT");
    Envio.resetVar("STRING");
  }

  @Test
  void linkToAnotherVariable() {
    Envio.setVar("ANOTHER_ENUM_VALUE", "THREE");
    Envio.setVar("ENUM_VALUE", "${ANOTHER_ENUM_VALUE}");

    Envio.setVar("ANOTHER_PRIMITIVE_INT", "10");
    Envio.setVar("PRIMITIVE_INT", "$ANOTHER_PRIMITIVE_INT");

    AllOptions options = OptionsFactory.create(AllOptions.class);
    assertThat(options.enumValue(), is(AllOptions.TestEnum.THREE));
    assertThat(options.primitiveInt(), is(10));
  }

}