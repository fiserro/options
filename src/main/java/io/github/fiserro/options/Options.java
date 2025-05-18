package io.github.fiserro.options;

import io.github.fiserro.options.extension.validation.ValidateOptionsException;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Interface for the Options object. The Options object is a container for the options of the
 * application. It is used to set and get the values of the options.
 */
public interface Options<T extends Options<T>> {

    /**
     * Returns the set of the option definitions.
     *
     * @return the set of the option definitions
     */
    Set<OptionDef> options();

    /**
     * Returns the value of the option by its key - name or alias.
     *
     * @param key the name or alias of the option
     * @return the value of the option or null if the option is not set and has no default value
     */
    Object getValue(String key);

    /**
     * Returns the value of the option by its key - name or alias.
     *
     * @param optionDef the option definition
     * @return the value of the option or null if the option is not set and has no default value
     */
    Object getValue(OptionDef optionDef);

    /**
     * Creates a new instance of the {@link OptionsBuilder} for the given options class. If you need
     * to modify some option you have to do it in the builder because the options are immutable. By
     * this you can create a different type of options with the same values.
     *
     * @param optionsClass the class of the options
     * @param <B>          the type of the builder
     * @return the options builder
     */
    <B extends OptionsBuilder<T, B>> OptionsBuilder<T, B> toBuilder(Class<T> optionsClass);

    /**
     * Creates a new instance of the {@link OptionsBuilder} for the same options class. If you need to
     * modify some option you have to do it in the builder because the options are immutable.
     *
     * @return the options builder
     * @see #toBuilder(Class)
     */
    <B extends OptionsBuilder<T, B>> OptionsBuilder<T, B> toBuilder();


    // TODO OptionDef key variant

    /**
     * Returns new instance of the options with changed value of the option.
     *
     * @param key   the name or alias of the option
     * @param value the value of the option
     * @return the new instance of the options
     */
    T withValue(String key, Object value);

    /**
     * Validates the options and returns the set of the constraint violations.
     *
     * @return the set of the constraint violations
     */
    Set<ConstraintViolation<T>> validate();

    /**
     * <p>Validates the options and returns itself if the options are valid.
     * If the options are not valid, it throws an {@link ValidateOptionsException}.</p>
     *
     * <p>You can use it this way:</p>
     * <code>
     * val options = OptionsFactory.createOptions(MyOptions.class, args)
     * .validated();
     * </code>
     *
     * @return the validated options
     */
    T validated() throws ValidateOptionsException;

    /**
     * Returns true if the options are valid.
     *
     * @return true if the options are valid
     */
    boolean isValid();
}
