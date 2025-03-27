package io.github.fiserro.options;

import com.google.common.base.CaseFormat;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;

/**
 * Represents the definition of an option.
 */
@Builder(toBuilder = true)
public record OptionDef(
    @Delegate Option option,
    @Delegate Method method,
    Method wither,
    Set<String> keys,
    OptionPath path,
    OptionDef parent,
    Set<OptionDef> children
) {

  /**
   * Returns the class of the options that the option is defined in.
   *
   * @return the class of the options that the option is defined in
   */
  public Class<?> optionsClass() {
    return method.getDeclaringClass();
  }

  /**
   * Returns the generic return type of the option.
   *
   * @param index the index of the generic return type
   * @return the generic return type of the option
   */
  public Class<?> getGenericReturnTypes(int index) {
    Type[] genericReturnTypes = getGenericReturnTypes();
    return (Class<?>) genericReturnTypes[index];
  }

  /**
   * Returns true if the option is a generic type.
   *
   * @return true if the option is a generic type
   */
  public boolean isGenericType() {
    return getGenericReturnTypes().length > 0;
  }

  /**
   * Returns the generic return types of the option.
   *
   * @return the generic return types of the option
   */
  public Type[] getGenericReturnTypes() {
    Type genericType = method.getGenericReturnType();
    if (genericType instanceof ParameterizedType parameterizedType) {
      return parameterizedType.getActualTypeArguments();
    }
    return new Type[0];
  }

  /**
   * Returns the Java type of the option.
   *
   * @return the Java type of the option
   */
  public Type javaType() {
    return method.getReturnType();
  }

  /**
   * Returns true if the {@link #javaType()} is an {@link Options} type.
   *
   * @return true if the {@link #javaType()} is an {@link Options} type
   */
  public boolean isOptionsType() {
    return Options.class.isAssignableFrom(classType());
  }

  /**
   * Returns the class type of the option.
   *
   * @return the class type of the option
   */
  public Class<?> classType() {
    Type type = javaType();
    if (type instanceof Class<?> c) {
      return c;
    }
    throw new IllegalArgumentException("Not a class type: " + type);
  }

  /**
   * Returns the wrapper type of the type if it is a primitive type. Otherwise, returns the class
   * type.
   *
   * @return the wrapper type of the type if it is a primitive type
   */
  public Class<?> wrapperType() {
    if (isPrimitive()) {
      if (javaType() == int.class) {
        return Integer.class;
      } else if (javaType() == boolean.class) {
        return Boolean.class;
      } else if (javaType() == double.class) {
        return Double.class;
      } else if (javaType() == float.class) {
        return Float.class;
      } else if (javaType() == long.class) {
        return Long.class;
      } else if (javaType() == short.class) {
        return Short.class;
      } else if (javaType() == byte.class) {
        return Byte.class;
      } else if (javaType() == char.class) {
        return Character.class;
      }
    }
    return classType();
  }

  /**
   * Returns the name of the option in Java.
   *
   * @return the name of the option in Java
   */
  public String name() {
    return method.getName();
  }

  /**
   * Returns the set of keys that can be used to access this option.
   *
   * @return the set of keys that can be used to access this option
   */
  public Set<String> keys() {
    Stream<String> defaultKeys = Stream.concat(
        Stream.of(option.env()),
        Stream.of(name(), CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name()))
    ).filter(s -> !s.isBlank());
    if (keys == null) {
      return defaultKeys.collect(Collectors.toSet());
    }
    return Stream.concat(defaultKeys, keys.stream()).collect(Collectors.toSet());
  }

  /**
   * Returns true if the two options are equal in terms of their type, required status, and value
   * separator.
   *
   * @param other the other option to compare to
   * @return true if the two options are equal in terms of their type, required status, and value
   * separator
   */
  public boolean strongEquals(OptionDef other) {
    return Objects.equals(javaType(), other.javaType()) &&
        // validations?
        Objects.equals(option.parser(), other.option.parser());
  }

  /**
   * Returns true if the option has a default value declared by default method or is a primitive
   * type.
   *
   * @return true if the option has a default value
   */
  public boolean hasDefaultValue() {
    return hasDefaultMethod() || isPrimitive();
  }

  /**
   * Returns true the method has default implementation.
   *
   * @return true if the option has a default value
   */
  public boolean hasDefaultMethod() {
    return method.isDefault();
  }

  public boolean hasWither() {
    return wither != null;
  }

  /**
   * Returns true if the option is a primitive type.
   *
   * @return true if the option is a primitive type
   */
  public boolean isPrimitive() {
    return javaType() instanceof Class<?> c && c.isPrimitive();
  }

  /**
   * Invokes the method of the option with the given options.
   *
   * @param options the options to invoke the method with
   * @return the result of the method invocation
   */
  @SneakyThrows
  Object invokeMethod(Options options) {
    return method.invoke(options);
  }

  /**
   * Returns the default value of the option, if the option is a primitive type, otherwise throws an
   * exception.
   *
   * @return the default value of the option
   */
  Object getDefaultPrimitiveValue() {
    if (javaType() == int.class) {
      return 0;
    } else if (javaType() == boolean.class) {
      return false;
    } else if (javaType() == double.class) {
      return 0.0;
    } else if (javaType() == float.class) {
      return 0.0f;
    } else if (javaType() == long.class) {
      return 0L;
    } else if (javaType() == short.class) {
      return (short) 0;
    } else if (javaType() == byte.class) {
      return (byte) 0;
    } else if (javaType() == char.class) {
      return '\u0000';
    } else {
      throw new IllegalArgumentException("Not a primitive type: " + javaType());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OptionDef that = (OptionDef) o;
    return Objects.equals(method.getName(), that.method.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(method.getName());
  }

  @Override
  public String toString() {
    return javaType().getTypeName() + " " + path();
  }
}
