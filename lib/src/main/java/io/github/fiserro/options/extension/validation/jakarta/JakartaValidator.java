package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.extension.validation.AbstractOptionsValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.val;

/**
 * Extension that validates if all required options are set.
 */
public class JakartaValidator extends AbstractOptionsValidator {

  public JakartaValidator(Class<? extends Options> declaringClass) {
    super(declaringClass);
  }

  @Override
  protected Set<ConstraintViolation<OptionDef>> validate(
      OptionsBuilder<? extends Options> options) {
    return options.options().values().stream()
        .distinct()
        .sorted(Comparator.comparing(OptionDef::name))
        .flatMap(option -> validate(options, option))
        .collect(Collectors.toSet());
  }

  private Stream<ConstraintViolation<OptionDef>> validate(
      OptionsBuilder<? extends Options> options, OptionDef option) {

    val value = getValue(options, option);
    return Stream.of(option.getAnnotations())
        .map(a -> switch (a) {
          case NotNull notNull -> notNull(option, value, notNull);
          case Size size -> size(option, value, size);
          default -> Optional.<ConstraintViolation<OptionDef>>empty();
        })
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  private Optional<ConstraintViolation<OptionDef>> size(OptionDef option, @Nullable Object value,
      Size size) {
    int sizeValue = switch (value) {
      case null -> 0;
      case Collection<?> c -> c.size();
      case Object[] a -> a.length;
      case String s -> s.length();
      default -> 0;
    };
    if (size.min() > sizeValue || size.max() < sizeValue) {
      return Optional.of(new ConstraintViolationImpl<>(size.message(), option.name(), value));
    }
    return Optional.empty();
  }

  private Optional<ConstraintViolation<OptionDef>> notNull(OptionDef option, Object value,
      NotNull notNull) {
    if (value != null) {
      return Optional.empty();
    }
    return Optional.of(new ConstraintViolationImpl<>(notNull.message(), option.name(), value));
  }

  private Object getValue(OptionsBuilder<? extends Options> options, OptionDef option) {
    try {
      return options.getValueOrPrimitiveDefault(option.name());
    } catch (Exception ignored) {
      // getting a value may fail if some dependencies are not set. in this case we consider the option as not set
      return null;
    }
  }

}
