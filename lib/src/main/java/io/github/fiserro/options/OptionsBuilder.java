package io.github.fiserro.options;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ClassUtils;

/**
 * Builder for the Options interface.
 *
 * @param <T> the type of the Options interface
 */
public class OptionsBuilder<T extends Options> {

  private static final String INVALID_KEY = "Invalid key: ";
  private final Class<T> optionsClass;
  private final Map<String, OptionDef> optionDefs;
  private final Map<String, Object> values;
  private final String[] args;

  private static final Set<String> RESERVED_METHOD_NAMES =
      Set.of("equals", "hashCode", "toString", "getClass", "setValue", "getValue", "toBuilder",
          "withValue");

  /**
   * Creates the options builder from the given Options Class, values and program arguments.
   *
   * @param optionsClass the class of the options
   * @param values       the values of the options
   * @param args         the program arguments
   * @param <T>          the type of the options
   * @return the options builder
   */
  public static <T extends Options> OptionsBuilder<T> newBuilder(Class<T> optionsClass,
      Map<String, Object> values, String... args) {
    Map<String, Object> valuesCopy = deepCopyMap(values);
    return new OptionsBuilder<>(optionsClass, valuesCopy, args);
  }

  @SuppressWarnings("unchecked")
  private static <K, V> Map<K, V> deepCopyMap(Map<K, V> map) {
    return map.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> (V) copyValue(e.getValue()), (a, b) -> a, HashMap::new
        ));
  }

  private static Object copyValue(Object value) {
    return switch (value) {
      case Options options -> options.toBuilder().values();
      case Map<?, ?> map -> deepCopyMap(map);
      case List<?> list -> list.stream().map(OptionsBuilder::copyValue).toList();
      case Set<?> set -> set.stream().map(OptionsBuilder::copyValue).collect(Collectors.toSet());
      default -> value;
    };
  }

  /**
   * Creates the options builder from the given Options Class, values and program arguments.
   *
   * @param optionsClass the class of the options
   * @param values       the values of the options
   * @param args         the program arguments
   */
  private OptionsBuilder(Class<T> optionsClass, Map<String, Object> values, String... args) {
    this.optionsClass = optionsClass;
    this.optionDefs = new OptionScanner().scanByName(optionsClass);
    optionDefs.forEach((k, v) -> {
      if (RESERVED_METHOD_NAMES.contains(k)) {
        throw new IllegalArgumentException("The option name " + k + " is reserved");
      }
      if (v.isOptionsType()) {
        Object nestedValues = values.remove(k);
        if (nestedValues == null) {
          nestedValues = new HashMap<>();
        } else if (!(nestedValues instanceof Map<?, ?>)) {
          throw new IllegalArgumentException("The nested values for " + k + " must be a Map");
        }
        OptionsBuilder<Options> nestedBuilder = new OptionsBuilder<>((Class<Options>) v.classType(),
            (Map<String, Object>) nestedValues, args);
        values.put(k, nestedBuilder);
      }
    });
    this.values = values;
    this.args = args;
  }

  /**
   * Sets the value of the option. The value must be of the same type as the option or String.
   *
   * @param key   the name of alias of the option
   * @param value the value of the option
   */
  public void setValue(String key, Object value) {
    OptionDef optionDef = optionsByKey().get(key);
    if (optionDef == null) {
      throw new IllegalArgumentException("No such option for key: " + key);
    }
    values.put(optionDef.name(), parse(optionDef, value));
  }

  public void setValue(Object value, String... path) {
    OptionsBuilder<?> builder = this;
    for (int i = 0; i < path.length - 1; i++) {
      String key = path[i];
      Object v = builder.getValueOrPrimitiveDefault(key);
      if (v instanceof OptionsBuilder<?> nestedBuilder) {
        builder = nestedBuilder;
      } else {
        throw new IllegalArgumentException(
            "Key: " + key + " on path: " + String.join(".", path) + " is not a nested option");
      }
    }
    builder.setValue(path[path.length - 1], value);
  }

  /**
   * Returns the value of the option by its key. The key is the java name of the option, the default
   * environment variable name or any of the environment name aliases.
   * <p>If method has primitive return type, it returns default value of the primitive type.
   *
   * @param key the name of the option
   * @return the value of the option, primitive default or null
   */
  public Object getValueOrPrimitiveDefault(String key) {
    OptionDef optionDef = optionsByKey().get(key);
    if (optionDef == null) {
      throw new IllegalArgumentException(INVALID_KEY + key);
    }
    Object value = values.get(optionDef.name());
    if (value == null && optionDef.isPrimitive()) {
      value = optionDef.getDefaultPrimitiveValue();
    }
    return value;
  }

  /**
   * Returns the value of the option by its key. The key is the java name of the option, the default
   * environment variable name or any of the environment name aliases.
   *
   * @param key the name of the option
   * @return the value of the option or null
   */
  public Object getValue(String key) {
    OptionDef optionDef = optionsByKey().get(key);
    if (optionDef == null) {
      throw new IllegalArgumentException(INVALID_KEY + key);
    }
    return values.get(optionDef.name());
  }

  /**
   * Returns values of the options in unmodifiableMap.
   *
   * @return the values of the options
   */
  public Map<String, Object> values() {
    return values;
  }

  /**
   * Resets the value of the option to its default value.
   *
   * @param key the name or alias of the option
   * @return the removed value of the option
   */
  public Object resetValue(String key) {
    OptionDef optionDef = optionsByKey().get(key);
    if (optionDef == null) {
      throw new IllegalArgumentException(INVALID_KEY + key);
    }
    return values.remove(optionDef.name());
  }

  /**
   * Returns the given program arguments the Options instance was created with.
   *
   * @return the program arguments
   */
  public String[] args() {
    return args;
  }

  /**
   * Returns the options definition collected in Map by its name.
   *
   * @return the options definition
   */
  public Map<String, OptionDef> options() {
    return optionDefs;
  }

  /**
   * Returns the options definition collected in Map by its keys.
   *
   * @return the options definition
   */
  public Map<String, OptionDef> optionsByKey() {
    return optionDefs.values().stream()
        .flatMap(o -> o.keys().stream().map(k -> Map.entry(k, o)))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Returns the class of the options interface.
   *
   * @return the class of the options interface
   */
  public Class<? extends Options> optionsInterface() {
    return optionsClass;
  }

  /**
   * Creates a new instance of the Options from the builder.
   *
   * @return the options instance
   */
  public T build() {
    return OptionsFactory.buildOptions(this);
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof OptionsBuilder<?> o) {
      return optionsClass.equals(o.optionsClass) && values.equals(o.values());
    }
    return false;
  }

  @SneakyThrows
  private Object parse(OptionDef optionDef, Object value) {
    if (value == null) {
      return null;
    } else if (optionDef.javaType() == value.getClass()) {
      return value;
    } else if (optionDef.javaType() instanceof Class<?> c && ClassUtils.isAssignable(
        value.getClass(), c, true)) {
      return value;
    } else if (value instanceof String s) {
      Class<? extends ValueParser> parser = optionDef.parser();
      return parser.getDeclaredConstructor().newInstance().parse((Class<?>) optionDef.javaType(),
          optionDef.getGenericReturnTypes(), s);
    } else {
      throw new IllegalArgumentException(
          "Cannot parse " + value.getClass() + " to " + optionDef.javaType());
    }
  }
}
