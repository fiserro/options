package io.github.fiserro.options.extension;

import io.github.fiserro.options.OptionsException;
import io.github.fiserro.options.extension.validation.AbstractOptionsValidator;
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
   * Internal record to pair an extension with its declaring class for logging purposes.
   */
  private record ExtensionInfo(OptionsExtension extension, Class<?> declaringClass) {}

  /**
   * Scans the given options class for extensions.
   *
   * @param optionsClass the options class
   * @return the Map of found extensions
   * @throws IllegalExtensionException if the options class declares more than one exclusive
   *                                   extension of the same type
   */
  public NavigableMap<OptionExtensionType, List<OptionsExtension>> scan(Class<?> optionsClass) {
    TreeMap<OptionExtensionType, List<ExtensionInfo>> extensionInfos = scanInternal(optionsClass);

    // Convert to the public return type (without declaring class info)
    TreeMap<OptionExtensionType, List<OptionsExtension>> result = new TreeMap<>();
    extensionInfos.forEach((type, infos) ->
        result.put(type, infos.stream().map(ExtensionInfo::extension).collect(Collectors.toList())));
    return result;
  }

  private TreeMap<OptionExtensionType, List<ExtensionInfo>> scanInternal(Class<?> optionsClass) {
    TreeMap<OptionExtensionType, List<ExtensionInfo>> extensions;

    if (optionsClass.isAnnotationPresent(OptionsExtensions.class)) {
      OptionsExtensions optionsExtensions = optionsClass.getAnnotation(OptionsExtensions.class);
      extensions = Stream.of(optionsExtensions.value())
          .map(c -> new ExtensionInfo(instantiateExtension(c), optionsClass))
          .collect(
              Collectors.groupingBy(info -> info.extension().type(), TreeMap::new, Collectors.toList()));

      extensions.forEach((type, v) -> {
        if (type.exclusive() && v.size() > 1) {
          throw new IllegalExtensionException(
              optionsClass.getSimpleName() + " declares more than one ("
                  + v.size() + ") exclusive extension of the same type " + type);
        }
      });
    } else {
      extensions = new TreeMap<>();
    }

    for (Class<?> i : optionsClass.getInterfaces()) {
      scanInternal(i).forEach((type, v) -> v.forEach(info -> {
        if (type.exclusive() && extensions.containsKey(type)) {
          logOverriddenOption(extensions.get(type).getFirst(), info);
        } else {
          extensions.computeIfAbsent(type, k -> new ArrayList<>()).add(info);
        }
      }));
    }
    return extensions;
  }

  private static void logOverriddenOption(ExtensionInfo preferred, ExtensionInfo other) {
    log.info("Extension {} defined on class {} is overridden by extension {} defined on class {}",
        other.extension().getClass().getSimpleName(), other.declaringClass().getSimpleName(),
        preferred.extension().getClass().getSimpleName(), preferred.declaringClass().getSimpleName());
  }

  @SneakyThrows
  private OptionsExtension instantiateExtension(Class<? extends OptionsExtension> extensionClass) {
    OptionsExtension extension = extensionClass.getDeclaredConstructor().newInstance();
    if (extension.type() == OptionExtensionType.VALIDATION &&
        !AbstractOptionsValidator.class.isAssignableFrom(extensionClass)) {
      throw new IllegalExtensionException("Validation extension must extend AbstractOptionsValidator");
    }
    return extension;
  }

  @StandardException
  public static class IllegalExtensionException extends OptionsException {

  }
}
