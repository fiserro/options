package io.github.fiserro.options.extension.validation.jakarta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.OptionPath;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.test.NestedCompositionOptions;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
        ),
        Arguments.of(
            NestedCompositionOptions.class,
            Map.of("source", Map.of("string", "$12")),
            Set.of(
                Pair.of(OptionPath.of("source", "string"), Pattern.class),
                Pair.of(OptionPath.of("source", "listOfString"), NotNull.class),
                Pair.of(OptionPath.of("target", "string"), NotNull.class),
                Pair.of(OptionPath.of("target", "listOfString"), NotNull.class)
            )
        )
    );
  }

  @MethodSource("arguments")
  @ParameterizedTest
  void exceptionIsThrownWhenRequiredOptionIsNotSet(Class<? extends Options> clazz,
      Map<String, Object> values,
      Set<Pair<Path, Class<Annotation>>> expectedViolations) {


    Options<?> options = OptionsFactory.create(clazz, values);
    assertThat(options.isValid(), is(expectedViolations.isEmpty()));

    Set<? extends ConstraintViolation<?>> violations = options.validate();

    Set<Pair<Path, Class<Annotation>>> violationsSimple = violations.stream()
        .map(c -> {
          Path path = c.getPropertyPath();
          Class<Annotation> constraint = (Class<Annotation>) c.getConstraintDescriptor().getAnnotation().annotationType();
          return Pair.of(path, constraint);
        })
        .collect(Collectors.toSet());

    expectedViolations.forEach(ev -> {
      assertThat(violationsSimple, hasItem(ev));
    });
    assertThat(violationsSimple.size(), is(expectedViolations.size()));
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
