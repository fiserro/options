package io.github.fiserro.options;


import static org.apache.commons.lang3.ClassUtils.isAssignable;

import com.github.sisyphsu.dateparser.DateParserUtils;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

/**
 * Default implementation of {@link ValueParser}
 */
public class ValueParserDefault implements ValueParser {

  private static final String ESCAPE_SEQUENCE = "\\\\";
  private static final String NOT_ESCAPED_BEHIND = "(?<!" + ESCAPE_SEQUENCE + ")";

  @Override
  public Object parse(Class<?> type, Type[] genericTypes, String stringValue) {
    if (Collection.class.isAssignableFrom(type)) {

      if (genericTypes.length != 1) {
        throw new IllegalArgumentException("Collection type must have exactly one generic type");
      }

      Collection<Object> value;
      if (type == Collection.class || type == List.class || type == ArrayList.class) {
        value = new ArrayList<>();
      } else if (type == Set.class) {
        value = new HashSet<>();
      } else {
        throw new IllegalArgumentException("Unsupported collection type " + type);
      }

      if (StringUtils.isBlank(stringValue)) {
        return value;
      }

      Class<?> innerType = (Class<?>) genericTypes[0];
      for (String sv : stringValue.split(NOT_ESCAPED_BEHIND + ",")) {
        sv = sv.replace(ESCAPE_SEQUENCE, "");
        value.add(parseValue(innerType, sv));
      }
      return value;
    } else if (Map.class.isAssignableFrom(type)) {
      throw new IllegalArgumentException("Map type is not supported yet");
    }
    return parseValue(type, stringValue);
  }

  @SneakyThrows
  private Object parseValue(Class<?> type, String stringValue) {
    Object value;
    if (type.isAssignableFrom(String.class)) {
      value = stringValue;
    } else if (isAssignable(type, Integer.class, true)) {
      value = Integer.valueOf(stringValue);
    } else if (isAssignable(type, Long.class, true)) {
      value = Long.valueOf(stringValue);
    } else if (isAssignable(type, Double.class, true)) {
      value = Double.valueOf(stringValue);
    } else if (isAssignable(type, Float.class, true)) {
      value = Float.valueOf(stringValue);
    } else if (isAssignable(type, Short.class, true)) {
      value = Short.valueOf(stringValue);
    } else if (isAssignable(type, Byte.class, true)) {
      value = Byte.valueOf(stringValue);
    } else if (isAssignable(type, Boolean.class, true)) {
      value = Boolean.valueOf(stringValue);
    } else if (type.isEnum()) {
      value = parseEnum(type, stringValue);
    } else if (type.isAssignableFrom(Date.class)) {
      value = DateParserUtils.parseDate(stringValue);
    } else if (type.isAssignableFrom(LocalDate.class)) {
      value = DateParserUtils.parseDateTime(stringValue).toLocalDate();
    } else if (type.isAssignableFrom(LocalDateTime.class)) {
      value = DateParserUtils.parseDateTime(stringValue);
    } else if (type.isAssignableFrom(OffsetDateTime.class)) {
      value = DateParserUtils.parseOffsetDateTime(stringValue);
    } else {
      value = type.getConstructor(String.class).newInstance(stringValue);
    }
    return value;
  }

  private Object parseEnum(Class<?> type, String stringValue) {
    Object value;
    List<?> ignoreCaseCandidates = Arrays.stream(type.getEnumConstants())
        .filter(e -> e.toString().equalsIgnoreCase(stringValue))
        .toList();
    if (ignoreCaseCandidates.isEmpty()) {
      throw new OptionsException(
          stringValue + " does not match any of the enum values of " + type.getSimpleName());
    } else if (ignoreCaseCandidates.size() > 1) {
      throw new OptionsException(
          stringValue + " matches multiple enum values of " + type.getSimpleName());
    } else {
      value = ignoreCaseCandidates.getFirst();
    }
    return value;
  }
}
