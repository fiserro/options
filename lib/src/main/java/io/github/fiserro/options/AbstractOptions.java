package io.github.fiserro.options;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The parent class of all options created with the {@link OptionsFactory}. It implements base set
 * of methods for the options.
 */
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"optionsClass", "values"})
public abstract class AbstractOptions<T extends Options> implements Options {

  private final Class<T> optionsClass;
  private final Map<String, Object> values;
  private final Map<String, OptionDef> options;

  @Override
  public Object getValue(String key) {
    OptionDef optionDef = options.get(key);
    if (optionDef == null) {
      throw new IllegalArgumentException("Invalid key: " + key);
    }
    try {
      return optionDef.method().invoke(this);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public T withValue(String key, Object value) {
    OptionsBuilder<T> builder = toBuilder();
    builder.setValue(key, value);
    return builder.build();
  }

  @Override
  public <X extends Options> OptionsBuilder<X> toBuilder(Class<X> optionsClass) {
    return OptionsBuilder.newBuilder(optionsClass, values);
  }

  /**
   * Returns the value from the Map of internal values by the option name. This method is used for
   * the internal purposes and should not be used in the application code.
   *
   * @param key the name of the option
   * @return the value of the option or null if the option is not set
   */
  Object getInternalValue(String key) {
    return values.get(key);
  }

  /**
   * Creates a new instance of the {@link OptionsBuilder} from these options. If you need to modify
   * some option you have to do it in the builder because the options are immutable.
   *
   * @return the options builder
   */
  public OptionsBuilder<T> toBuilder() {
    return toBuilder(optionsClass);
  }

  @Override
  public String toString() {
    return optionsClass.getSimpleName() + '@' + Integer.toHexString(hashCode()) + "{"
        + valuesAsString() + "}";
  }

  private String valuesAsString() {
    return options.values().stream()
        .distinct()
        .map(option -> Pair.of(option.name(), getValue(option.name())))
        .filter(pair -> pair.getValue() != null)
        .sorted(Entry.comparingByKey())
        .map(pair -> pair.getKey() + "=" + pair.getValue())
        .collect(Collectors.joining(", "));
  }
}
