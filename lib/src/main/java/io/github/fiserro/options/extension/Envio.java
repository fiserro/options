package io.github.fiserro.options.extension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * Utility class for getting environment variables.
 */
@UtilityClass
public class Envio {

  private static final Map<String, String> overrides = Collections.synchronizedMap(new HashMap<>());

  /**
   * Returns the value of the environment variable. If the variable is not set, it returns
   * <code>null</code>.
   *
   * @param name name of the property
   * @return value of the property
   */
  public static synchronized String getVar(String name) {
    if (overrides.containsKey(name)) {
      return overrides.get(name);
    }
    return System.getenv(name);
  }

  public static synchronized void setVar(String name, String value) {
    overrides.put(name, value);
  }

  /**
   * Puts <code>null</code> to overrides so {@link Envio} returns <code>null</code> even if
   * {@link System#getenv(String)} returns a value
   *
   * @param name name of the property
   * @return value of the property
   */
  public static synchronized String resetVar(String name) {
    return overrides.put(name, null);
  }

  /**
   * Puts <code>value</code> to overrides only if the property is not already set
   *
   * @param name  name of the property
   * @param value value of the property
   */
  public static synchronized void setVarIfMissing(String name, String value) {
    if (getVar(name) == null) {
      overrides.putIfAbsent(name, value);
    }
  }

  public static void clear() {
    overrides.clear();
  }

}
