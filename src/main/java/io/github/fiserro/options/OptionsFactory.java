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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Singular;
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
     * Creates the options instance with the given dynamic extensions and program arguments.
     *
     * @param optionsClass       the class of the options
     * @param dynamicExtensions  the dynamic extensions to apply
     * @param args               the program arguments
     * @param <T>                the type of the options
     * @return the options instance
     */
    public static <T extends Options<T>> T create(Class<T> optionsClass,
            List<OptionsExtension> dynamicExtensions, String... args) {
        return create(optionsClass, new HashMap<>(), dynamicExtensions, args);
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
     * @return the options instance
     */
    public static <T extends Options<T>> T create(
            Class<T> optionsClass, Map<String, Object> values, String... args) {
        return create(optionsClass, values, Collections.emptyList(), args);
    }

    /**
     * Creates the options instance with the given values, dynamic extensions, and program arguments.
     * You can prefill the options with the values. Those value will have the lowest possible priority
     * comparing to all standard value-setting extensions. It means the prefill values will be
     * overwritten by the values from any other source. Fill the values is the easiest way how to set
     * the values of the options programmatically without setting the environment variables etc.
     *
     * @param optionsClass       the class of the options
     * @param values             the values of the options
     * @param dynamicExtensions  the dynamic extensions to apply
     * @param args               the program arguments
     * @param <T>                the type of the options
     * @return the options instance
     */
    @lombok.Builder(builderClassName = "$OptionsBuilder")
    public static <T extends Options<T>> T create(
            Class<T> optionsClass, @Singular Map<String, Object> values, @Singular List<OptionsExtension> dynamicExtensions,
            String... args) {
        OptionsBuilder<T, ?> optionsBuilder = OptionsBuilder.newBuilder(optionsClass, values, dynamicExtensions, args);
        return buildOptions(optionsBuilder);
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
    static <T extends Options<T>, B extends OptionsBuilder<T, B>> T buildOptions(
            OptionsBuilder<T, B> optionsBuilder) {

        applyExtensions(optionsBuilder);

        Builder<?> builder = new ByteBuddy()
                .subclass(AbstractOptions.class)
                .implement(optionsBuilder.optionsInterface());

        for (OptionDef optionDef : optionsBuilder.options()) {
            if (optionsBuilder.getValue(optionDef) == null && optionDef.hasDefaultMethod()) {
                // do not intercept default methods when the value is not set
                continue;
            }
            Object value = optionsBuilder.getValueOrPrimitiveDefault(optionDef);
            if (optionDef.isOptionsType()) {
                optionsBuilder.setValue(optionDef, buildOptions((OptionsBuilder<?, ?>) value));
                value = optionsBuilder.getValueOrPrimitiveDefault(optionDef);
            }
            if (value != null && !optionsBuilder.values().containsKey(optionDef)) {
                optionsBuilder.setValue(optionDef, value);
            }

            Preconditions.checkState(
                    value == null || optionDef.wrapperType().isAssignableFrom(value.getClass()),
                    "The value of the option %s is not of the expected type %s but it is %s",
                    optionDef.name(),
                    optionDef.javaType(), value == null ? null : value.getClass());
            builder = builder.method(named(optionDef.name()).and(takesNoArguments()))
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
                    Map.class, List.class);
            //noinspection unchecked
            return (T) constructor.newInstance(optionsBuilder.optionsInterface(), optionsBuilder.values(),
                    optionsBuilder.optionsByKey(), optionsBuilder.dynamicExtensions());
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
    public static <T extends Options<T>> T clone(T options) {
        return options.toBuilder().build();
    }

    /**
     * Validates the options.
     *
     * @param options the options to be validated
     */
    public static <T extends Options<T>> Set<ConstraintViolation<T>> validate(
            Options<T> options) {
        OptionsBuilder<T, ?> builder = options.toBuilder();
        return validate(options, builder);
    }


    /**
     * Validates the options.
     *
     * @param builder the options builder to be validated
     */
    private static <T extends Options<T>> Set<ConstraintViolation<T>> validate(
            Options<T> options, OptionsBuilder<?, ?> builder) {

        Stream<ConstraintViolation<T>> childrenViolations = builder.values().entrySet().stream()
                .filter(e -> e.getKey().isOptionsType())
                .map(e -> (OptionsBuilder<?, ?>) e.getValue())
                .flatMap(subBuilder -> validate(options, subBuilder).stream());

        // Get annotation-based validation extensions
        Stream<ConstraintViolation<T>> annotationViolations = new OptionExtensionScanner().scan(
                        builder.optionsInterface())
                .getOrDefault(OptionExtensionType.VALIDATION, List.of())
                .stream()
                .map(e -> (AbstractOptionsValidator<T>) e)
                .flatMap(e -> e.validate(options, builder).stream());

        // Get dynamic validation extensions
        Stream<ConstraintViolation<T>> dynamicViolations = builder.dynamicExtensions().stream()
                .filter(e -> e.type() == OptionExtensionType.VALIDATION)
                .map(e -> (AbstractOptionsValidator<T>) e)
                .flatMap(e -> e.validate(options, builder).stream());

        return Stream.concat(
                        childrenViolations,
                        Stream.concat(annotationViolations, dynamicViolations))
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
        Map<OptionExtensionType, List<OptionsExtension>> annotationExtensions = scanner.scan(
                optionsBuilder.optionsInterface());

        // Merge dynamic extensions with annotation-based extensions
        Map<OptionExtensionType, List<OptionsExtension>> allExtensions = new TreeMap<>(annotationExtensions);

        for (OptionsExtension dynamicExt : optionsBuilder.dynamicExtensions()) {
            OptionExtensionType type = dynamicExt.type();
            List<OptionsExtension> existing = allExtensions.computeIfAbsent(type, k -> new ArrayList<>());

            // Validate exclusivity: cannot mix annotation-based and dynamic extensions of the same exclusive type
            if (type.exclusive() && !existing.isEmpty()) {
                throw new OptionExtensionScanner.IllegalExtensionException(
                        "Cannot add dynamic extension of exclusive type " + type +
                                " when annotation-based extension already exists for interface " +
                                optionsBuilder.optionsInterface().getSimpleName());
            }

            existing.add(dynamicExt);
        }

        // Validate no duplicate exclusive types in dynamic extensions
        optionsBuilder.dynamicExtensions().stream()
                .filter(e -> e.type().exclusive())
                .collect(Collectors.groupingBy(OptionsExtension::type))
                .forEach((type, list) -> {
                    if (list.size() > 1) {
                        throw new OptionExtensionScanner.IllegalExtensionException(
                                "Multiple dynamic extensions of exclusive type " + type +
                                        " for interface " + optionsBuilder.optionsInterface().getSimpleName());
                    }
                });

        // Apply extensions in reverse order (highest priority last)
        Stream.of(OptionExtensionType.values())
                .sorted(Comparator.reverseOrder())
                .filter(allExtensions::containsKey)
                .filter(type -> type != OptionExtensionType.VALIDATION)
                .map(allExtensions::get)
                .flatMap(List::stream)
                .forEach(extension -> extension.extend(optionsBuilder));
    }

}