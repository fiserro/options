package io.github.fiserro.options.extension.validation.jakarta;

import jakarta.validation.ConstraintValidator;
import java.lang.annotation.Annotation;

/**
 * Jakarta validator resolver.
 */
public interface JakartaValidatorResolver {

  /**
   * Get the validator for the given annotation and value.
   *
   * @param annotation the annotation
   * @param value the value
   * @return the validator
   */
  ConstraintValidator<Annotation, Object> getValidator(Annotation annotation,
      Object value);
}
