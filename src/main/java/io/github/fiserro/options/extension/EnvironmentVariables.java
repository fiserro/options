package io.github.fiserro.options.extension;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extension that sets the value of the option from the environment variable. It internally uses
 * {@link Envio} to get the value of the environment variable.
 */
public class EnvironmentVariables extends OptionsExtensionSettingValues {

  public EnvironmentVariables(Class<? extends Options<?>> declaringClass) {
    super("environment variable", OptionExtensionType.LOAD_FROM_ENV, declaringClass);
  }

  @Override
  public void extend(OptionsBuilder<?, ?> options) {
    getNestedKeys(options).forEach((env, path) -> {
      String value = Envio.getVar(env);
      if (value != null && value.startsWith("$")) {
        if (value.matches("^\\$\\{.*}$")) {
          value = Envio.getVar(value.substring(2, value.length() - 1));
        } else {
          value = Envio.getVar(value.substring(1));
        }
      }
      setValue(options, value, env, path);
    });
  }

  protected Map<String, String[]> getNestedKeys(OptionsBuilder<?, ?> options) {
    return options.options().stream()
        .flatMap(o -> {
          if (o.isOptionsType()) {
            return getNestedKeys(
                (OptionsBuilder<?, ?>) options.getValueOrPrimitiveDefault(o.name()))
                .keySet()
                .stream()
                .flatMap(n -> o.keys().stream().map(k -> k + "__" + n));
          } else {
            return o.keys().stream();
          }
        })
        .collect(Collectors.toMap(k -> k, k -> k.split("__")));
  }
}
