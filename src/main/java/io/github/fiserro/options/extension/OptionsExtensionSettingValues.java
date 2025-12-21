package io.github.fiserro.options.extension;

import com.google.common.base.Preconditions;
import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.OptionsBuilder;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract superclass for extension setting values of the options. Unifies the info message about
 * changing the value of the option.
 */
@Slf4j
public abstract class OptionsExtensionSettingValues extends AbstractOptionsExtension {

  private final String sourceType;

  protected OptionsExtensionSettingValues(String sourceType, OptionExtensionType type) {
    super(type);
    this.sourceType = sourceType;
  }

  /**
   * Sets the value of the option and logs the info message if the value is changed. Null values are
   * not allowed to set and are ignored.
   *
   * @param optionsBuilder the options
   * @param value          the value to set
   * @param sourceName     the name of the source of the value
   * @param path           nested key path to the option
   */
  protected void setValue(OptionsBuilder<?, ?> optionsBuilder, Object value,
      String sourceName, String... path) {
    if (value == null) {
      return;
    }
    String key = path[0];
    OptionDef optionDef = optionsBuilder.optionsByKey().get(key);
    if (optionDef == null) {
      log.warn("The value: {} cannot be set. No such option for key: {} in path: {}", value, key,
          Arrays.toString(path));
      return;
    }
    if (path.length > 1) {
      Preconditions.checkState(optionDef.isOptionsType(),
          "Nested values are allowed only for options of type Options");
      Object builderValue = optionsBuilder.getValueOrPrimitiveDefault(key);
      if (builderValue instanceof OptionsBuilder<?, ?> nestedBuilder) {
        setValue(nestedBuilder, value, sourceName, Arrays.copyOfRange(path, 1, path.length));
      } else {
        throw new IllegalArgumentException(
            "The value: " + value + " cannot be set. OptionsBuilder for key: " + key + " in path: "
                + Arrays.toString(path)
                + " is not properly initialized");
      }
    } else {
      Object previousStateValue = tryGetValue(optionsBuilder, key);
      if (optionsBuilder.values().get(optionDef) != null) {
        // if the value is already set, it means that the value is not changed
        return;
      }
      optionsBuilder.setValue(optionDef.name(), value);
      Object currentStateValue = tryGetValue(optionsBuilder, key);
      if (!Objects.equals(previousStateValue, currentStateValue)) {
        log.info("'{}' option is set to '{}' from {} '{}'", optionDef.name(), value, sourceType, sourceName);
      }
    }
  }

  /**
   * Tries to get the value of the option. If it fails, returns null. The only purpose is to check
   * the old value before setting the new one to avoid unnecessary console logs when the value is
   * the same. Sometimes the value cannot be retrieved because it has default implementation
   * depending on other missing option. It swallows the exception and returns null in this case to
   * prevent failing the whole extension settings.
   *
   * @param options the options
   * @param key     key to the option
   * @return the value of the option or null
   */
  private Object tryGetValue(OptionsBuilder<?, ?> options, String key) {
    try {
      return options.getValueOrPrimitiveDefault(key);
    } catch (Exception ignored) {
      return null;
    }
  }
}