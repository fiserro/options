package io.github.fiserro.options.extension;

import io.github.fiserro.options.OptionsBuilder;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helper class for loading option values from environment variables.
 * This class is used by both {@link EnvironmentVariables} and {@link EnvironmentFile}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnvironmentVariableLoader {

  /**
   * Loads environment variables and sets them as option values using the provided value setter.
   *
   * @param options     the options builder to get keys from
   * @param valueSetter callback that receives (value, envName, path) for each matched env var
   */
  public static void loadAndSetValues(OptionsBuilder<?, ?> options, ValueSetter valueSetter) {
    getNestedKeys(options).forEach((env, path) -> {
      String value = Envio.getVar(env);
      if (value != null && value.startsWith("$")) {
        if (value.matches("^\\$\\{.*}$")) {
          value = Envio.getVar(value.substring(2, value.length() - 1));
        } else {
          value = Envio.getVar(value.substring(1));
        }
      }
      valueSetter.setValue(value, env, path);
    });
  }

  private static Map<String, String[]> getNestedKeys(OptionsBuilder<?, ?> options) {
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

  /**
   * Functional interface for setting values.
   */
  @FunctionalInterface
  public interface ValueSetter {
    void setValue(String value, String envName, String[] path);
  }
}