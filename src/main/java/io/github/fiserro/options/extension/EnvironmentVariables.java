package io.github.fiserro.options.extension;

import io.github.fiserro.options.OptionsBuilder;

/**
 * Extension that sets the value of the option from the environment variable. It internally uses
 * {@link Envio} to get the value of the environment variable.
 */
public class EnvironmentVariables extends OptionsExtensionSettingValues {

  public EnvironmentVariables() {
    super("environment variable", OptionExtensionType.LOAD_FROM_ENV);
  }

  @Override
  public void extend(OptionsBuilder<?, ?> options) {
    EnvironmentVariableLoader.loadAndSetValues(options,
        (value, env, path) -> setValue(options, value, env, path));
  }
}
