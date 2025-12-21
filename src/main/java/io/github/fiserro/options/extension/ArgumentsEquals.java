package io.github.fiserro.options.extension;

import io.github.fiserro.options.OptionsBuilder;
import java.util.stream.Stream;

/**
 * Extension that sets the value of the option from given program arguments.
 * <p>The argument must be in the form of: <pre><b>--name=value</b></pre>
 * <p>For nested options, the path is separated by dots:<pre><b>--nested.path=value</b></pre>
 */
public class ArgumentsEquals extends OptionsExtensionSettingValues {

  public ArgumentsEquals() {
    super("program argument", OptionExtensionType.LOAD_FROM_ARGS);
  }

  @Override
  public void extend(OptionsBuilder<?, ?> options) {
    Stream.of(options.args())
        .forEach(arg -> {
          if (!arg.startsWith("--")) {
            throw new IllegalArgumentException("Invalid argument: " + arg);
          }
          String[] split = arg.split("=", 2);
          String path = split[0].substring(2);
          setValue(options, split[1], split[0], path.split("\\."));
        });
  }
}
