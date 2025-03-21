package io.github.fiserro.options;

/**
 * Interface for the Options object. The Options object is a container for the options of the
 * application. It is used to set and get the values of the options.
 */
public interface Options {

  /**
   * Returns the value of the option by its key - name or alias.
   *
   * @param key the name or alias of the option
   * @return the value of the option or null if the option is not set and has no default value
   */
  Object getValue(String key);

  /**
   * Creates a new instance of the {@link OptionsBuilder} for the given options class. If you need
   * to modify some option you have to do it in the builder because the options are immutable. By
   * this you can create a different type of options with the same values.
   *
   * @param optionsClass the class of the options
   * @return the options builder
   */
  <T extends Options> OptionsBuilder<T> toBuilder(Class<T> optionsClass);

  /**
   * Creates a new instance of the {@link OptionsBuilder} for the same options class. If you need to
   * modify some option you have to do it in the builder because the options are immutable.
   *
   * @return the options builder
   * @see #toBuilder(Class)
   */
  <T extends Options> OptionsBuilder<T> toBuilder();

  /**
   * Returns new instance of the options with changed value of the option.
   *
   * @param key   the name or alias of the option
   * @param value the value of the option
   * @return the new instance of the options
   */
  <T extends Options> T withValue(String key, Object value);
}
