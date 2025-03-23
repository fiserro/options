package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.OptionScanner;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.test.OptionsAll;
import io.github.fiserro.options.test.OptionsDateTime;
import io.github.fiserro.options.test.OptionsDependentDefaults;
import io.github.fiserro.options.test.OptionsDuplicates;
import io.github.fiserro.options.test.OptionsEnum;
import io.github.fiserro.options.test.OptionsEnum.TestEnum;
import io.github.fiserro.options.test.OptionsIntegers;
import io.github.fiserro.options.test.OptionsLongs;
import io.github.fiserro.options.test.OptionsOverridingDefaults;
import io.github.fiserro.options.test.OptionsStrings;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(PER_CLASS)
class GeneralOptionsTest {

  /**
   * List of options classes to test.
   */
  private final List<Class<? extends Options>> optionsClasses = List.of(
      OptionsIntegers.class,
      OptionsLongs.class,
      OptionsDateTime.class,
      OptionsStrings.class,
      OptionsEnum.class,
      OptionsDependentDefaults.class,
      OptionsDuplicates.class,
      OptionsOverridingDefaults.class,
      OptionsAll.class
  );

  /**
   * Sample values for each type of option.
   */
  private final List<Object> sampleValues = Stream.of(
          10, 20, 30, 10L, 20L, 30L,
          "test/test", "integration/test", "test/integration",
          TestEnum.ONE, TestEnum.TWO, TestEnum.THREE,
          LocalDate.of(2021, 1, 1),
          LocalDate.of(2021, 2, 2),
          Pair.of("2024-02-18T23:11:55Z", new Date(1708297915000L)),
          Pair.of("2024-02-18T23:11:56Z", new Date(1708297916000L)),
          OffsetDateTime.of(2024, 2, 18, 23, 11, 55, 0, ZoneOffset.UTC),
          OffsetDateTime.of(2024, 2, 18, 23, 11, 56, 0, ZoneOffset.UTC)
      )
      .map(Object.class::cast)
      .toList();

  @ParameterizedTest(name = "{0} {2}")
  @MethodSource("optionCases")
  void checkOption(String stringValue, Object expectedValue, OptionDef option,
      OptionsBuilder<?> optionsBuilder) {

    assertThat("Every option in the " + optionsBuilder
            + " has to be tested but there is no sample value for "
            + option.javaType() + ":" + option.name(),
        expectedValue, is(notNullValue()));

    Map<String, Object> values = optionsBuilder.values();
    optionsBuilder.resetValue(option.name());

    Options options = optionsBuilder.build();

    if (option.hasDefaultValue()) {
      assertThat("After reset, default value must be returned", options.getValue(option.name()),
          notNullValue());
      assertThat("After reset, default value must be returned", option.invokeMethod(options),
          notNullValue());
    } else {
      assertThat("After reset, no value must be returned", options.getValue(option.name()),
          nullValue());
      assertThat("After reset, no value must be returned", option.invokeMethod(options),
          nullValue());
    }

    options = options.withValue(option.name(), expectedValue);

    assertThat("After setting value, it's possible to read it", options.getValue(option.name()),
        is(expectedValue));
    assertThat("After setting value, it's possible to read it", option.invokeMethod(options),
        is(expectedValue));
    assertThat("After setting value, source map contains it",
        options.toBuilder(optionsBuilder.optionsInterface()).values().get(option.name()),
        is(expectedValue));

    optionsBuilder.resetValue(option.name());
    optionsBuilder.setValue(option.name(), stringValue);
    options = optionsBuilder.build();

    assertThat("After setting a string representation of value, it's possible to read it",
        options.getValue(option.name()), is(expectedValue));
    assertThat("After setting a string representation of value, it's possible to read it",
        option.invokeMethod(options), is(expectedValue));
    assertThat("After setting a string representation of value, source map contains it",
        values.get(option.name()), is(expectedValue));
  }

  Stream<Arguments> optionCases() {
    return optionsClasses.stream()
        .flatMap(optionsClass -> {
          OptionsTestHelper<?> helper = new OptionsTestHelper<>(optionsClass);
          return helper.optionCases();
        });
  }

  private class OptionsTestHelper<T extends Options> {

    private final OptionsBuilder<T> optionsBuilder;
    private final Map<String, OptionDef> optionsByName;

    private OptionsTestHelper(Class<T> optionsClass) {

      optionsBuilder = OptionsFactory.newBuilder(optionsClass, new HashMap<>());
      optionsByName = new OptionScanner().scanByName(optionsClass);
    }

    Stream<Arguments> optionCases() {
      List<Arguments> arguments = optionsByName.values().stream()
          .map(option -> {
            Pair<String, Object> valuePair = getValuePair(option);
            optionsBuilder.setValue(option.name(), valuePair.getValue());
            return Arguments.of(valuePair.getKey(), valuePair.getValue(), option, optionsBuilder);
          })
          .toList();
      return arguments.stream();
    }

    private Pair<String, Object> getValuePair(OptionDef option) {

      Map<? extends Class<?>, List<Pair<String, Object>>> samplePairs = sampleValues.stream()
          .map(v -> {
            if (v instanceof Pair<?, ?> pair) {
              //noinspection unchecked
              return (Pair<String, Object>) pair;
            } else {
              return Pair.of(v.toString(), v);
            }
          })
          .collect(Collectors.groupingBy(pair -> pair.getValue().getClass()));

      List<Pair<String, Object>> pairs;
      if (Collection.class.isAssignableFrom(option.wrapperType())) {
        if (!option.isGenericType()) {
          throw new IllegalArgumentException("Collection option must have generic type");
        }
        pairs = samplePairs.get(option.getGenericReturnTypes(0));
      } else {
        pairs = samplePairs.get(option.wrapperType());
      }
      if (pairs == null || pairs.isEmpty()) {
        return Pair.of(null, null);
      }

      if (List.class.isAssignableFrom(option.wrapperType())) {
        return Pair.of(
            pairs.stream().map(Pair::getKey).collect(Collectors.joining(",")),
            pairs.stream().map(Pair::getValue).toList()
        );
      } else if (Set.class.isAssignableFrom(option.wrapperType())) {
        return Pair.of(
            pairs.stream().map(Pair::getKey).collect(Collectors.joining(",")),
            pairs.stream().map(Pair::getValue).collect(Collectors.toSet())
        );
      } else {
        return pairs.getFirst();
      }
    }
  }
}
