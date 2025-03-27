package io.github.fiserro.options;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

import com.google.common.base.Preconditions;
import io.github.fiserro.options.extension.OptionExtensionScanner;
import io.github.fiserro.options.extension.OptionExtensionType;
import io.github.fiserro.options.extension.OptionsExtension;
import io.github.fiserro.options.extension.validation.AbstractOptionsValidator;
import jakarta.validation.ConstraintViolation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Factory for creating options instances.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptionsFactory {

  /**
   * Creates the options instance with the given program arguments.
   *
   * @param optionsClass the class of the options
   * @param args         the program arguments
   * @param <T>          the type of the options
   * @return the options instance
   */
  public static <T extends Options<T>> T create(Class<T> optionsClass, String... args) {
    return create(optionsClass, new HashMap<>(), args);
  }

  /**
   * Creates the options instance with the given values and program arguments. You can prefill the
   * options with the values. Those value will have the lowest possible priority comparing to all
   * standard value-setting extensions. It means the prefill values will be overwritten by the
   * values from any other source. Fill the values is  the easiest way how to set the values of the
   * options programmatically without setting the environment variables etc.
   *
   * @param optionsClass the class of the options
   * @param values       the values of the options
   * @param args         the program arguments
   * @param <T>          the type of the options
   * @param <B>          the type of the builder
   * @return the options instance
   */
  public static <T extends Options<T>, B extends OptionsBuilder<T, B>> T create(
      Class<T> optionsClass, Map<String, Object> values, String... args) {
    OptionsBuilder<T, B> optionsBuilder = newBuilder(optionsClass, values, args);
    return buildOptions(optionsBuilder);
  }

  /**
   * Creates the options builder from the given Options Class, values and program arguments.
   *
   * @param optionsClass the class of the options
   * @param values       the values of the options
   * @param args         the program arguments
   * @param <T>          the type of the options
   * @param <B>          the type of the builder
   * @return the options builder
   */
  public static <T extends Options<T>, B extends OptionsBuilder<T, B>> OptionsBuilder<T, B> newBuilder(
      Class<T> optionsClass, Map<String, Object> values, String... args) {
    return OptionsBuilder.newBuilder(optionsClass, values, args);
  }

  /**
   * Creates the options from the given Options Builder. Before the options are created, the
   * extensions are applied to the builder.
   *
   * @param optionsBuilder the options builder
   * @param <T>            the type of the options
   * @param <B>            the type of the builder
   * @return the options builder
   */
  @SneakyThrows
  public static <T extends Options<T>, B extends OptionsBuilder<T, B>> T buildOptions(
      OptionsBuilder<T, B> optionsBuilder) {

    applyExtensions(optionsBuilder);

    Builder<?> builder = new ByteBuddy()
        .subclass(AbstractOptions.class)
        .implement(optionsBuilder.optionsInterface());

    for (OptionDef optionDef : optionsBuilder.options()) {
      String name = optionDef.name();
      if (optionsBuilder.getValue(name) == null && optionDef.hasDefaultMethod()) {
        // do not intercept default methods when the value is not set
        continue;
      }
      Object value = optionsBuilder.getValueOrPrimitiveDefault(name);
      if (optionDef.isOptionsType()) {
        optionsBuilder.setValue(name, buildOptions((OptionsBuilder<?, ?>) value));
        value = optionsBuilder.getValueOrPrimitiveDefault(name);
      }
      if (value != null && !optionsBuilder.values().containsKey(name)) {
        optionsBuilder.values().put(name, value);
      }

      Preconditions.checkState(
          value == null || optionDef.wrapperType().isAssignableFrom(value.getClass()),
          "The value of the option %s is not of the expected type %s but it is %s", name,
          optionDef.javaType(), value == null ? null : value.getClass());
      builder = builder.method(named(name).and(takesNoArguments()))
          .intercept(MethodDelegation.to(GetValueInterceptor.class));

      if (optionDef.hasWither()) {
        builder = builder.method(named(optionDef.wither().getName())
                .and(ElementMatchers.takesArguments(1)))
            .intercept(MethodDelegation.to(WithValueInterceptor.class));
      }
    }

    try (Unloaded<?> unloaded = builder.make()) {
      Class<?> dynamicType = unloaded.load(Thread.currentThread().getContextClassLoader())
          .getLoaded();
      Constructor<?> constructor = dynamicType.getDeclaredConstructor(Class.class, Map.class,
          Map.class);
      //noinspection unchecked
      return (T) constructor.newInstance(optionsBuilder.optionsInterface(), optionsBuilder.values(),
          optionsBuilder.optionsByKey());
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class GetValueInterceptor {

    @RuntimeType
    public static Object intercept(@This AbstractOptions<?> self, @Origin Method method) {
      return self.getInternalValue(method.getName());
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class WithValueInterceptor {

    @RuntimeType
    public static Object intercept(@This AbstractOptions<?> self,
        @Origin Method method,
        @AllArguments Object[] args) {
      String name = OptionScanner.nameOfWither(method);
      return self.withValue(name, args[0]);
    }
  }


  /**
   * Clones the options instance.
   *
   * @param options the options instance
   * @param <T>     the type of the options
   * @return the cloned options instance
   */
  @SneakyThrows
  public static <T extends Options<T>, B extends OptionsBuilder<T, B>> T clone(T options) {
    return options.toBuilder().build();
  }

  /**
   * Validates the options.
   *
   * @param options the options to be validated
   */
  public static <T extends Options<T>, B extends OptionsBuilder<T, B>> Set<ConstraintViolation<T>> validate(
      Options<T> options) {
    OptionsBuilder<T, B> builder = options.toBuilder();
    NavigableMap<OptionExtensionType, List<OptionsExtension>> extensions = new OptionExtensionScanner().scan(
        builder.optionsInterface());
    return extensions.getOrDefault(
            OptionExtensionType.VALIDATION, List.of())
        .stream()
        .map(e -> (AbstractOptionsValidator<T, B>) e)
        .flatMap(e -> e.validate(builder, options).stream())
        .collect(Collectors.toSet());
  }

  /**
   * Applies the extensions to the options builder.
   *
   * @param optionsBuilder the options builder
   */
  private static <T extends Options<T>, B extends OptionsBuilder<T, B>> void applyExtensions(
      OptionsBuilder<T, B> optionsBuilder) {
    OptionExtensionScanner scanner = new OptionExtensionScanner();
    Map<OptionExtensionType, List<OptionsExtension>> extensions = scanner.scan(
        optionsBuilder.optionsInterface());

    // In the first run we omit the validation extension to possibly load unit name or file name which could be used in the second run
    // It is ugly, but it was the fast and easy way how to solve the problem and for now, I do not have a better idea
    Stream.of(OptionExtensionType.values())
        .filter(extensions::containsKey)
        .filter(type -> type != OptionExtensionType.VALIDATION)
        .map(extensions::get)
        .flatMap(List::stream)
        .forEach(extension -> extension.extend(optionsBuilder));

//    Stream.of(OptionExtensionType.values())
//        .filter(extensions::containsKey)
//        .map(extensions::get)
//        .flatMap(List::stream)
//        .forEach(extension -> extension.extend(optionsBuilder));
  }

}