package io.github.fiserro.options;

import java.lang.reflect.Type;

/**
 * Interface for parsing string values to objects. It is used by {@link OptionsFactory} to parse
 * values from environment variables, program arguments or configuration files.
 */
@FunctionalInterface
public interface ValueParser {

  /**
   * Parses the string value to the object of the given type.
   *
   * @param type         the type of the object
   * @param genericTypes the generic types of the object
   * @param value        the string value
   * @return the object of the given type
   */
  Object parse(Class<?> type, Type[] genericTypes, String value);
}
