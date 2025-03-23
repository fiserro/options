package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.extension.validation.AbstractOptionsValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArray;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfBoolean;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfByte;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfChar;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfInt;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfLong;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfShort;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCollection;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForMap;

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
        .map(a -> validate(a, options, option, value))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  private Optional<ConstraintViolation<OptionsBuilder<?>>> validate(Annotation annotation,
      OptionsBuilder<? extends Options> options, OptionDef option, Object value) {
    ConstraintValidator<Annotation, Object> validator = getValidator(annotation, value);
    if (validator.isValid(value, null)) {
      return Optional.empty();
    }
    val message = String.format("Option '%s' is not valid: %s", option.name(),
        annotation.annotationType().getSimpleName());
    return Optional.of(new ConstraintViolationImpl(message, value, option, options, annotation));
  }

  private ConstraintValidator<Annotation, Object> getValidator(Annotation annotation,
      Object value) {
    ConstraintValidator<?, ?> validator = switch (annotation) {
      case NotNull notNull -> new NotNullValidator();
      case Size size -> switch (value) {
        case null -> (v, c) -> true;
        case Object[] array -> new SizeValidatorForArray();
        case boolean[] array -> new SizeValidatorForArraysOfBoolean();
        case byte[] array -> new SizeValidatorForArraysOfByte();
        case char[] array -> new SizeValidatorForArraysOfChar();
        case double[] array -> new SizeValidatorForArraysOfDouble();
        case float[] array -> new SizeValidatorForArraysOfFloat();
        case int[] array -> new SizeValidatorForArraysOfInt();
        case long[] array -> new SizeValidatorForArraysOfLong();
        case short[] array -> new SizeValidatorForArraysOfShort();
        case CharSequence charSequence -> new SizeValidatorForCharSequence();
        case Collection<?> collection -> new SizeValidatorForCollection();
        case Map<?, ?> map -> new SizeValidatorForMap();
        default -> (v, c) -> true;
      };
      case DecimalMax decimalMax -> switch (value) {
        case null -> (v, c) -> true;
        case BigDecimal b -> new DecimalMaxValidatorForBigDecimal();
        case BigInteger b -> new DecimalMaxValidatorForBigInteger();
        case Byte b -> new DecimalMaxValidatorForByte();
        case CharSequence c -> new DecimalMaxValidatorForCharSequence();
        case Double d -> new DecimalMaxValidatorForDouble();
        case Float f -> new DecimalMaxValidatorForFloat();
        case Integer i -> new DecimalMaxValidatorForInteger();
        case Long l -> new DecimalMaxValidatorForLong();
        case Short s -> new DecimalMaxValidatorForShort();
        case Number n -> new DecimalMaxValidatorForNumber();
        default -> (v, c) -> true;
      };
      case DecimalMin decimalMin -> switch (value) {
        case null -> (v, c) -> true;
        case BigDecimal b -> new DecimalMinValidatorForBigDecimal();
        case BigInteger b -> new DecimalMinValidatorForBigInteger();
        case Byte b -> new DecimalMinValidatorForByte();
        case CharSequence c -> new DecimalMinValidatorForCharSequence();
        case Double d -> new DecimalMinValidatorForDouble();
        case Float f -> new DecimalMinValidatorForFloat();
        case Integer i -> new DecimalMinValidatorForInteger();
        case Long l -> new DecimalMinValidatorForLong();
        case Short s -> new DecimalMinValidatorForShort();
        case Number n -> new DecimalMinValidatorForNumber();
        default -> (v, c) -> true;
      };
      default -> (v, c) -> true;
    };
    @SuppressWarnings("unchecked")
    ConstraintValidator<Annotation, Object> typedValidator = (ConstraintValidator<Annotation, Object>) validator;
    typedValidator.initialize(annotation);
    return typedValidator;
  }

  private Object getValue(OptionsBuilder<? extends Options> options, OptionDef option) {
    try {
      return options.getValueOrPrimitiveDefault(option.name());
    } catch (Exception ignored) {
      // getting a value may fail if some dependencies are not set. in this case we consider the option as not set
      return null;
    }
  }
//
//  interface JakartaAnnotationValidator {
//
//    Optional<ConstraintViolation<OptionsBuilder<?>>> validate(
//        OptionsBuilder<? extends Options> options, OptionDef option, Object value);
//  }
//
//  @SuperBuilder
//  @RequiredArgsConstructor
//  public abstract static class AbstractJakartaAnnotationValidator<A extends Annotation> implements
//      JakartaAnnotationValidator {
//
//    protected final A annotation;
//  }

}
