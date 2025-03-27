package io.github.fiserro.options;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;

/**
 * Builder for the Options interface.
 *
 * @param <T> the type of the Options interface
 */
public class OptionsBuilder<T extends Options<T>, B extends OptionsBuilder<T, B>> {

  private static final String INVALID_KEY = "Invalid key: ";
  private final Class<T> optionsClass;
  private final Set<OptionDef> optionDefs;
  private final Map<OptionDef, Object> values;
  private final String[] args;

  /**
   * Creates the options builder from the given Options Class, values and program arguments.
   *
   * @param optionsClass the class of the options
   * @param optionDefs   the options definition
   * @param values       the values of the options
   * @param <T>          the type of the options
   * @param <B>          the type of the builder
   * @return the options builder
   */
  public static <T extends Options<T>, B extends OptionsBuilder<T, B>> OptionsBuilder<T, B> newBuilder(
      Class<T> optionsClass, Collection<OptionDef> optionDefs, Map<OptionDef, Object> values) {
    Map<OptionDef, Object> valuesCopy = deepCopyMap(values);
    return new OptionsBuilder<>(optionsClass, optionDefs, valuesCopy);
  }

  /**
   * Creates the options builder from the given Options Class, values and program arguments.
   *
   * @param optionsClass the class of the options
   * @param values       the values of the options
   * @param <T>          the type of the options
   * @param <B>          the type of the builder
   * @return the options builder
   */
  public static <T extends Options<T>, B extends OptionsBuilder<T, B>> OptionsBuilder<T, B> newBuilder(
      Class<T> optionsClass, Map<String, Object> values, String... args) {
    Map<String, Object> valuesCopy = deepCopyMap(values);
    Set<OptionDef> optionDefSet = new OptionScanner().scan(optionsClass);
    return new OptionsBuilder<>(optionsClass, optionDefSet, valuesCopy, args);
  }

  /**
   * Deep copies the map.
   *
   * @param map the map to copy
   * @param <K> the type of the key
   * @param <V> the type of the value
   * @return the copied map
   */
  @SuppressWarnings("unchecked")
  private static <K, V> Map<K, V> deepCopyMap(Map<K, V> map) {
    return map.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> (V) copyValue(e.getValue()), (a, b) -> a, HashMap::new
        ));
  }

  /**
   * Deep copies the value.
   *
   * @param value the value to copy
   * @return the copied value
   */
  private static Object copyValue(Object value) {
    return switch (value) {
      case Options<?> options -> options.toBuilder();
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
   * @param optionDefs   the options definition
   * @param values       the values of the options
   */
  private OptionsBuilder(Class<T> optionsClass, Collection<OptionDef> optionDefs,
      Map<OptionDef, Object> values) {
    this.optionsClass = optionsClass;
    this.optionDefs = new HashSet<>(optionDefs);
    this.values = values;
    this.args = new String[0];
  }

  /**
   * Creates the options builder from the given Options Class, values and program arguments.
   *
   * @param optionsClass the class of the options
   * @param optionDefs   the options definition
   * @param values       the values of the options
   * @param args         the program arguments
   */
  private OptionsBuilder(Class<T> optionsClass, Set<OptionDef> optionDefs,
      Map<String, Object> values, String... args) {
    this.optionsClass = optionsClass;
    this.optionDefs = optionDefs;
    this.values = new HashMap<>();
    this.args = args;

    optionDefs.forEach(optionDef -> {
      Object value = values.get(optionDef.name());

      if (optionDef.isOptionsType()) {
        if (value == null) {
          value = new HashMap<>();
        }
        if (value instanceof Map<?, ?> map) {
          this.values.put(optionDef, new OptionsBuilder(optionDef.classType(), optionDef.children(), map, args));
        } else {
          throw new IllegalArgumentException(
              "The nested values for '" + optionDef.path() + "' must be a Map");
        }
      } else if (value != null) {
        this.values.put(optionDef, values.get(optionDef.name()));
      }
    });
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
    setValue(optionDef, value);
  }

  /**
   * Sets the value of the option. The value must be of the same type as the option or String.
   *
   * @param optionDef the option definition
   * @param value     the value of the option
   */
  public void setValue(OptionDef optionDef, Object value) {
    if (optionDef == null) {
      throw new IllegalArgumentException("OptionDef cannot be null");
    }
    values.put(optionDef, parse(optionDef, value));
  }

  /**
   * Sets the value of the nested option. The value must be of the same type as the option or
   * String.
   *
   * @param value the value of the option
   * @param path  the path to the nested option
   */
  public void setValue(Object value, String... path) {
    OptionsBuilder<?, ?> builder = this;
    for (int i = 0; i < path.length - 1; i++) {
      String key = path[i];
      Object v = builder.getValueOrPrimitiveDefault(key);
      if (v instanceof OptionsBuilder<?, ?> nestedBuilder) {
        builder = nestedBuilder;
      } else {
        throw new IllegalArgumentException(
            "Key: " + key + " on path: " + String.join(".", path) + " is not a nested option");
      }
    }
    builder.setValue(path[path.length - 1], value);
  }

  /**
   * Sets the value of the option. The value must be of the same type as the option or String.
   *
   * @param key   the name of alias of the option
   * @param value the value of the option
   */
  public OptionsBuilder<T, B> withValue(String key, Object value) {
    setValue(key, value);
    return this;
  }

  /**
   * Sets the value of the nested option. The value must be of the same type as the option or
   * String.
   *
   * @param value the value of the option
   * @param path  the path to the nested option
   */
  public OptionsBuilder<T, B> withValue(Object value, String... path) {
    setValue(value, path);
    return this;
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
    return getValueOrPrimitiveDefault(optionDef);
  }

  /**
   * Returns the value of the option by its definition. The key is the java name of the option, the
   * default environment variable name or any of the environment name aliases.
   * <p>If method has primitive return type, it returns default value of the primitive type.
   *
   * @param optionDef the option definition
   * @return the value of the option, primitive default or null
   */
  public Object getValueOrPrimitiveDefault(OptionDef optionDef) {
    if (optionDef == null) {
      throw new IllegalArgumentException("OptionDef cannot be null");
    }
    Object value = values.get(optionDef);
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
    return getValue(optionDef);
  }

  /**
   * Returns the value of the option by its definition.
   *
   * @param optionDef the option definition
   * @return the value of the option or null
   */
  public Object getValue(OptionDef optionDef) {
    if (optionDef == null) {
      throw new IllegalArgumentException("OptionDef cannot be null");
    }
    return values.get(optionDef);
  }

  /**
   * Returns values of the options in unmodifiableMap.
   *
   * @return the values of the options
   */
  public Map<OptionDef, Object> values() {
    return values;
  }

  /**
   * Resets the value of the option to its default value.
   *
   * @param key the name or alias of the option
   * @return the removed value of the option
   */
  public Object resetValue(String key) {
    // TODO OptionDef key variant for all methods
    OptionDef optionDef = optionsByKey().get(key);
    if (optionDef == null) {
      throw new IllegalArgumentException(INVALID_KEY + key);
    }
    return values.remove(optionDef);
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
   * Returns the options definition.
   *
   * @return the options definition
   */
  public Set<OptionDef> options() {
    return optionDefs;
  }

  /**
   * Returns the options definition collected in Map by its name.
   *
   * @return the options definition
   */
  public Map<String, OptionDef> optionsByName() {
    return optionDefs.stream()
        .collect(Collectors.toMap(OptionDef::name, o -> o));
  }

  /**
   * Returns the options definition collected in Map by its keys.
   *
   * @return the options definition
   */
  public Map<String, OptionDef> optionsByKey() {
    return optionDefs.stream()
        .flatMap(o -> o.keys().stream().map(k -> Map.entry(k, o)))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Returns the class of the options interface.
   *
   * @return the class of the options interface
   */
  public Class<T> optionsInterface() {
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
    if (other instanceof OptionsBuilder<?, ?> o) {
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
