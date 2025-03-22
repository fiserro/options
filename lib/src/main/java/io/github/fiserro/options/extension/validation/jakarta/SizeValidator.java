package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.extension.validation.jakarta.JakartaValidator.AbstractJakartaAnnotationValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Size;
import java.util.Collection;
import java.util.Optional;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SizeValidator extends AbstractJakartaAnnotationValidator<Size> {

  @Override
  public Optional<ConstraintViolation<OptionDef>> validate(OptionDef option, Object value) {
    int sizeValue = switch (value) {
      case null -> 0;
      case Collection<?> c -> c.size();
      case Object[] a -> a.length;
      case String s -> s.length();
      default -> 0;
    };
    if (annotation.min() > sizeValue || annotation.max() < sizeValue) {
      return Optional.of(
          new ConstraintViolationImpl<>(option, annotation.message(), value, option, null));
    }
    return Optional.empty();
  }
}
