package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.extension.validation.AbstractOptionsValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintViolation;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Extension that validates if all required options are set.
 */
@Slf4j
public class JakartaValidator<T extends Options<T>> extends AbstractOptionsValidator<T> {

  private final JakartaValidatorResolver validatorResolver = new HibernateValidatorResolver();

  public JakartaValidator(Class<? extends Options<?>> declaringClass) {
    super(declaringClass);
  }

  @Override
  public Set<ConstraintViolation<T>> validate(Options<T> options, OptionsBuilder<?, ?> builder) {
    return builder.options().stream()
        .distinct()
        .sorted(Comparator.comparing(OptionDef::name))
        .flatMap(option -> validate(options, builder, option))
        .collect(Collectors.toSet());
  }

  private Stream<ConstraintViolation<T>> validate(Options<T> options, OptionsBuilder<?, ?> builder,
      OptionDef option) {

    val value = getValue(builder, option);
    return Stream.of(option.getAnnotations())
        .map(a -> validate(a, options, option, value))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  private Optional<ConstraintViolation<T>> validate(Annotation annotation,
      Options<T> options, OptionDef option, Object value) {
    ConstraintValidator<Annotation, Object> validator = validatorResolver.getValidator(annotation,
        value);
    if (validator.isValid(value, null)) {
      return Optional.empty();
    }

    val message = String.format("@%s %s = %s - %s",
        annotation.annotationType().getSimpleName(), option, value, annotation);
    return Optional.of(new ConstraintViolationImpl(message, value, option, options, annotation));
  }

  private Object getValue(OptionsBuilder<?, ?> options, OptionDef option) {
    try {
      return options.getValueOrPrimitiveDefault(option.name());
    } catch (Exception ignored) {
      // getting a value may fail if some dependencies are not set. in this case we consider the option as not set
      return null;
    }
  }
}
