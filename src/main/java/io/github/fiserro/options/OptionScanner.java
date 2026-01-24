package io.github.fiserro.options;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Scans the given options interface and returns the set of options defined by the interface and its
 * super interfaces.
 */
@Slf4j
public class OptionScanner {

  private static final Set<String> RESERVED_METHOD_NAMES =
      Set.of("equals", "hashCode", "toString", "getClass", "setValue", "getValue", "toBuilder",
          "withValue");

  private void loadWithers(Class<?> clazz, Map<String, Method> withers) {
    Stream.of(clazz.getDeclaredMethods())
        .filter(m -> m.getName().startsWith("with"))
        .filter(m -> m.getParameterCount() == 1)  // Valid withers have exactly 1 parameter
        .filter(m -> !withers.containsKey(nameOfWither(m)))
        .forEach(m -> withers.put(nameOfWither(m), m));
  }

  /**
   * Recursively loads all wither methods from the class hierarchy.
   * This ensures withers from parent interfaces are available when scanning child options.
   */
  private void loadAllWithers(Class<?> clazz, Map<String, Method> withers) {
    loadWithers(clazz, withers);
    for (Class<?> parent : clazz.getInterfaces()) {
      loadAllWithers(parent, withers);
    }
  }

  public static String nameOfWither(Method wither) {
    String name = wither.getName().substring(4);
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
  }

  Set<OptionDef> scan(Class<?> clazz) {
    // Load all withers from entire hierarchy first, so they're available when scanning child options
    Map<String, Method> withers = new HashMap<>();
    loadAllWithers(clazz, withers);
    return scan(clazz, withers, OptionPath.empty());
  }

  private OptionDef optionDef(Method getter, Method wither, OptionPath path) {
    Preconditions.checkState(getter.isAnnotationPresent(Option.class),
        Option.class.getSimpleName() + " must be present on getter method");
    Preconditions.checkState(getter.getParameterCount() == 0,
        "Getter method: %s must have no parameters but has: %d", getter.getName(),
        getter.getParameterCount());
    if (wither != null) {
      Preconditions.checkState(wither.getParameterCount() == 1,
          "Wither method: %s must have exactly one parameter but has: %d", wither.getName(),
          wither.getParameterCount());
      Preconditions.checkState(
          isAssignable(getter.getReturnType(), wither.getParameterTypes()[0], true),
          "Wither method: %s parameter must have the same type as getter return type: %s but has: %s",
          wither.getName(), getter.getReturnType(), wither.getParameterTypes()[0]);
      Preconditions.checkState(Options.class.isAssignableFrom(wither.getReturnType()),
          "Wither method: %s parameter type: %s must be assignable to Options",
          wither.getName(), wither.getParameterTypes()[0]);
    }
    if (RESERVED_METHOD_NAMES.contains(getter.getName())) {
      throw new IllegalArgumentException("The option name " + getter.getName() + " is reserved");
    }

    Set<OptionDef> children;
    if (Options.class.isAssignableFrom(getter.getReturnType())) {
      children = scan(getter.getReturnType(), new HashMap<>(), path);
    } else {
      children = Set.of();
    }

    Option option = getter.getAnnotation(Option.class);
    return OptionDef.builder()
        .option(option)
        .method(getter)
        .wither(wither)
        .path(path)
        .children(children)
        .annotations(Stream.of(getter.getAnnotations()).toList())
        .build();
  }

  private Set<OptionDef> scan(Class<?> clazz, Map<String, Method> withers, OptionPath path) {

    if (!clazz.isInterface()) {
      throw new IllegalArgumentException("Options class must be an interface");
    }

    loadWithers(clazz, withers);

    Map<String, OptionDef> declaredOptions = Stream.of(clazz.getDeclaredMethods())
        .filter(m -> {
          if (m.isAnnotationPresent(Option.class) && m.getParameterCount() > 0) {
            log.warn("Option method {} in {} has to have no parameters", m.getName(),
                clazz.getSimpleName());
          }
          return m.isAnnotationPresent(Option.class);
        })
        .map(m -> optionDef(m, withers.get(m.getName()), path.add(m.getName())))
        .collect(Collectors.toMap(OptionDef::name, o -> o));

    Map<String, OptionDef> parentOptions = Stream.of(clazz.getInterfaces())
        .flatMap(i -> scan(i, withers, path).stream())
        .flatMap(o -> o.keys().stream().map(k -> Map.entry(k, o)))
        // Handle parent/child name conflict by skipping - parent option has lower priority
        .filter(o -> !declaredOptions.containsKey(o.getKey()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> {
          // Handle name conflict from same level of parent interfaces
          // Options with the same name must be strongly equal and their keys(aliases) are merged
          if (!a.strongEquals(b)) {
            throw new IllegalArgumentException(
                "Option " + a.name() + " is defined multiple times  in "
                    + a.optionsClass().getSimpleName() + " and " + b.optionsClass().getSimpleName()
                    + ". When the same option is defined multiple times, "
                    + "it has to have the same type, required status and value separator");
          }
          return a.toBuilder()
              .keys(Stream.concat(a.keys().stream(), b.keys().stream()).collect(Collectors.toSet()))
              .annotations(Stream.concat(a.annotations().stream(), b.annotations().stream())
                  .distinct()
                  .toList())
              .build();
        }));

    return Stream.concat(declaredOptions.values().stream(), parentOptions.values().stream())
        .collect(Collectors.toSet());
  }

  @VisibleForTesting
  Map<String, OptionDef> scanByKeys(Class<? extends Options> clazz) {
    return scan(clazz).stream()
        .flatMap(o -> o.keys().stream().map(k -> Map.entry(k, o)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public Map<String, OptionDef> scanByName(Class<? extends Options> clazz) {
    return scan(clazz).stream()
        .collect(Collectors.toMap(OptionDef::name, o -> o));
  }
}
