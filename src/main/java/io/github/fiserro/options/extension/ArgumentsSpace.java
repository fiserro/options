package io.github.fiserro.options.extension;

import io.github.fiserro.options.OptionsBuilder;

/**
 * Extension that sets the value of the option from given program arguments. The arguments must be
 * in the form of "--name", "value".
 */
public class ArgumentsSpace extends OptionsExtensionSettingValues {

  public ArgumentsSpace() {
    super("program argument", OptionExtensionType.LOAD_FROM_ARGS);
  }

  @Override
  public void extend(OptionsBuilder<?, ?> options) {
    String[] args = options.args();
    for (int i = 0; i < args.length; i += 2) {
      String argName = args[i];
      String argValue = args[i + 1];
      if (!argName.startsWith("--")) {
        throw new IllegalArgumentException("Invalid argument: " + argName);
      }
      String path = argName.substring(2);
      setValue(options, argValue, argName, path.split("\\."));
    }
  }
}
