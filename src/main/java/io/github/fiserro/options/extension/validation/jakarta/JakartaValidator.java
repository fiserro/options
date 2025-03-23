package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.extension.validation.AbstractOptionsValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Extension that validates if all required options are set.
 */
@Slf4j
public class JakartaValidator extends AbstractOptionsValidator {

  public JakartaValidator(Class<? extends Options> declaringClass) {
    super(declaringClass);
  }

  @Override
  protected Set<ConstraintViolation<OptionsBuilder<?>>> validate(
      OptionsBuilder<? extends Options> options) {
    return options.options().stream()
        .distinct()
        .sorted(Comparator.comparing(OptionDef::name))
        .flatMap(option -> validate(options, option))
        .collect(Collectors.toSet());
  }

  private Stream<ConstraintViolation<OptionsBuilder<?>>> validate(
      OptionsBuilder<? extends Options> options, OptionDef option) {

    val value = getValue(options, option);
    return Stream.of(option.getAnnotations())
        .map(a -> validator(a).validate(options, option, value))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  private JakartaAnnotationValidator validator(Annotation annotation) {
    return switch (annotation) {
      case NotNull notNull -> NotNullValidator.builder().annotation(notNull).build();
      case Size size -> SizeValidator.builder().annotation(size).build();
      case DecimalMax decimalMax -> DecimalMaxValidator.builder().annotation(decimalMax).build();
      default -> new AbstractJakartaAnnotationValidator<>(annotation) {
        @Override
        public Optional<ConstraintViolation<OptionsBuilder<?>>> validate(
            OptionsBuilder<? extends Options> options, OptionDef option, Object value) {
          log.warn("Annotation {} is not supported", annotation.annotationType().getSimpleName());
          return Optional.empty();
        }
      };
    };
  }

  private Object getValue(OptionsBuilder<? extends Options> options, OptionDef option) {
    try {
      return options.getValueOrPrimitiveDefault(option.name());
    } catch (Exception ignored) {
      // getting a value may fail if some dependencies are not set. in this case we consider the option as not set
      return null;
    }
  }

  interface JakartaAnnotationValidator {

    Optional<ConstraintViolation<OptionsBuilder<?>>> validate(
        OptionsBuilder<? extends Options> options, OptionDef option, Object value);
  }

  @SuperBuilder
  @RequiredArgsConstructor
  public abstract static class AbstractJakartaAnnotationValidator<A extends Annotation> implements
      JakartaAnnotationValidator {

    protected final A annotation;
  }

}
