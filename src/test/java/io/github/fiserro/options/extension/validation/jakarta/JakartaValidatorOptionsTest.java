package io.github.fiserro.options.extension.validation.jakarta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.OptionPath;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JakartaValidatorOptionsTest {

  static Stream<Arguments> arguments() {
    return Stream.of(
        Arguments.of(
            JakartaValidatedTestOptions.class,
            Map.of("bool", true),
            Set.of()
        ),
        Arguments.of(
            JakartaValidatedTestOptions.class,
            Map.of(),
            Set.of(
                Pair.of(OptionPath.of("bool"), NotNull.class)
            )
        ),
        Arguments.of(
            JakartaValidatedTestOptions.class,
            Map.of("min10", 5, "max10", 15),
            Set.of(
                Pair.of(OptionPath.of("min10"), DecimalMin.class),
                Pair.of(OptionPath.of("max10"), DecimalMax.class),
                Pair.of(OptionPath.of("bool"), NotNull.class)
            )
        )
    );
  }

  @MethodSource("arguments")
  @ParameterizedTest
  void exceptionIsThrownWhenRequiredOptionIsNotSet(Class<? extends Options> clazz,
      Map<String, Object> values,
      Set<ConstraintViolation<Pair<Path, Class<? extends Annotation>>>> expectedViolations) {

    Options<?> options = OptionsFactory.create(clazz, values);
    assertThat(options.isValid(), is(expectedViolations.isEmpty()));

    Set<? extends ConstraintViolation<?>> violations = options.validate();

    Set<? extends Pair<Path, ?>> constraintViolations = violations.stream()
        .map(c -> Pair.of(c.getPropertyPath(),
            c.getConstraintDescriptor().getAnnotation().annotationType()))
        .collect(Collectors.toSet());
    assertThat(constraintViolations, Is.is(expectedViolations));
  }

  @Test
  void simpleTest() {
    JakartaValidatedTestOptions options = OptionsFactory.create(
        JakartaValidatedTestOptions.class);
    assertThat(options.isValid(), is(false));
    Set<ConstraintViolation<JakartaValidatedTestOptions>> violations = options.validate();
    assertThat(violations.size(), is(1));
    ConstraintViolation<JakartaValidatedTestOptions> violation = violations.iterator().next();
    assertThat(violation.getPropertyPath(), is(OptionPath.of("bool")));
    assertThat(violation.getRootBean(), is(options));
    assertThat(violation.getRootBeanClass(), is(JakartaValidatedTestOptions.class));
    assertThat(violation.getMessage(), Is.is("@NotNull java.lang.Boolean bool = null - @jakarta.validation.constraints.NotNull(message=\"{jakarta.validation.constraints.NotNull.message}\", payload={}, groups={})"));
  }
}
