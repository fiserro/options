package io.github.fiserro.options.extension;

import io.github.fiserro.options.OptionsException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.experimental.StandardException;
import lombok.extern.slf4j.Slf4j;

/**
 * Scanner for finding options extensions on the given options class.
 */
@Slf4j
public class OptionExtensionScanner {

  /**
   * Scans the given options class for extensions.
   *
   * @param optionsClass the options class
   * @return the Map of found extensions
   * @throws IllegalExtensionException if the options class declares more than one exclusive
   *                                   extension of the same type
   */
  public NavigableMap<OptionExtensionType, List<OptionsExtension>> scan(Class<?> optionsClass) {

    TreeMap<OptionExtensionType, List<OptionsExtension>> extensions;

    if (optionsClass.isAnnotationPresent(OptionsExtensions.class)) {
      OptionsExtensions optionsExtensions = optionsClass.getAnnotation(OptionsExtensions.class);
      extensions = Stream.of(optionsExtensions.value())
          .map(c -> instantiateExtension(optionsClass, c))
          .collect(
              Collectors.groupingBy(OptionsExtension::type, TreeMap::new, Collectors.toList()));

      extensions.forEach((type, v) -> v.forEach(extension -> {
        if (type.exclusive() && v.size() > 1) {
          throw new IllegalExtensionException(
              optionsClass.getSimpleName() + " declares more than one ("
                  + v.size() + ") exclusive extension of the same type " + type);
        }
      }));
    } else {
      extensions = new TreeMap<>();
    }

    for (Class<?> i : optionsClass.getInterfaces()) {
      scan(i).forEach((type, v) -> v.forEach(extension -> {
        if (type.exclusive() && extensions.containsKey(type)) {
          logOverriddenOption(extensions.get(type).getFirst(), extension);
        } else {
          extensions.computeIfAbsent(type, k -> new ArrayList<>()).add(extension);
        }
      }));
    }
    return extensions;
  }

  private static void logOverriddenOption(OptionsExtension preferred, OptionsExtension other) {
    log.info("Extension {} defined on class {} is overridden by extension {} defined on class {}",
        other.getClass().getSimpleName(), other.declaringClass().getSimpleName(),
        preferred.getClass().getSimpleName(), preferred.declaringClass().getSimpleName());
  }

  @SneakyThrows
  private OptionsExtension instantiateExtension(Class<?> optionsClass,
      Class<? extends OptionsExtension> extensionClass) {
    Constructor<? extends OptionsExtension> constructor = extensionClass.getDeclaredConstructor(
        Class.class);
    return constructor.newInstance(optionsClass);
  }

  @StandardException
  public static class IllegalExtensionException extends OptionsException {

  }
}
