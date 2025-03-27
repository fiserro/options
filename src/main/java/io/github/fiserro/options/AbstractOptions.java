package io.github.fiserro.options;

import jakarta.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
public abstract class AbstractOptions<T extends AbstractOptions<T>> implements Options<T> {

  private final Class<T> optionsClass;
  private final Map<OptionDef, Object> values;
  private final Map<String, OptionDef> options;

  @Override
  public Set<OptionDef> options() {
    return new HashSet<>(options.values());
  }

  @Override
  public Object getValue(String key) {
    OptionDef optionDef = options.get(key);
    if (optionDef == null) {
      throw new IllegalArgumentException("Invalid key: " + key);
    }
    return getValue(optionDef);
  }

  @Override
  public Object getValue(OptionDef optionDef) {
    if (optionDef == null) {
      throw new IllegalArgumentException("OptionDef cannot be null");
    }
    try {
      return optionDef.method().invoke(this);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public T withValue(String key, Object value) {
    return toBuilder()
        .withValue(key, value)
        .build();
  }

  @Override
  public <B extends OptionsBuilder<T, B>> OptionsBuilder<T, B> toBuilder(Class<T> optionsClass) {
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
  public <B extends OptionsBuilder<T, B>> OptionsBuilder<T, B> toBuilder() {
    return toBuilder(optionsClass);
  }

  @Override
  public Set<ConstraintViolation<T>> validate() {
    return OptionsFactory.validate(this);
  }

  @Override
  public boolean isValid() {
    Set<ConstraintViolation<T>> validate = validate();
    return validate.isEmpty();
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
