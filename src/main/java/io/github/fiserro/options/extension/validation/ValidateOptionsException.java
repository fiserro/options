package io.github.fiserro.options.extension.validation;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.OptionsException;
import jakarta.validation.ConstraintViolation;

import java.util.Comparator;
import java.util.Set;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Exception thrown when options validation fails.
 * This exception contains the set of constraint violations and the options builder that caused the
 * validation to fail.
 */
@Getter
@Accessors(fluent = true)
public class ValidateOptionsException extends OptionsException {

    private final transient Set<ConstraintViolation<?>> validation;

    public ValidateOptionsException(Set<ConstraintViolation<?>> validation, Options<?> options) {
        super(getMessage(validation), options);
        this.validation = validation;
    }

    private static String getMessage(Set<ConstraintViolation<?>> validation) {
        int size = validation.size();
        return size + " options validation failed:\n" + validation.stream()
                .sorted(Comparator.comparing(ConstraintViolation::getMessage))
                .map(ConstraintViolation::getMessage)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }
}
