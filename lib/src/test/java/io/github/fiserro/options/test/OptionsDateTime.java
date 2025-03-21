package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface OptionsDateTime extends Options {

  Date DEFAULT_DATE = new Date();
  LocalDate DEFAULT_LOCAL_DATE = LocalDate.now();
  OffsetDateTime DEFAULT_OFFSET_DATE_TIME = OffsetDateTime.now();

  @Option(description = "Date option")
  Date date();

  @Option
  default Date dateWithDefault() {
    return DEFAULT_DATE;
  }

  @Option
  LocalDate localDate();

  @Option
  default LocalDate localDateWithDefault() {
    return DEFAULT_LOCAL_DATE;
  }

  @Option
  OffsetDateTime offsetDateTime();

  @Option
  default OffsetDateTime offsetDateTimeWithDefault() {
    return DEFAULT_OFFSET_DATE_TIME;
  }

  @Option
  List<Date> listOfDate();

  @Option
  default List<Date> listOfDateWithDefault() {
    return List.of(DEFAULT_DATE);
  }

  @Option
  List<LocalDate> listOfLocalDate();

  @Option
  default List<LocalDate> listOfLocalDateWithDefault() {
    return List.of(DEFAULT_LOCAL_DATE);
  }

  @Option
  List<OffsetDateTime> listOfOffsetDateTime();

  @Option
  default List<OffsetDateTime> listOfOffsetDateTimeWithDefault() {
    return List.of(DEFAULT_OFFSET_DATE_TIME);
  }

  @Option
  Set<Date> setOfDate();

  @Option
  default Set<Date> setOfDateWithDefault() {
    return Set.of(DEFAULT_DATE);
  }

  @Option
  Set<LocalDate> setOfLocalDate();

  @Option
  default Set<LocalDate> setOfLocalDateWithDefault() {
    return Set.of(DEFAULT_LOCAL_DATE);
  }

  @Option
  Set<OffsetDateTime> setOfOffsetDateTime();

  @Option
  default Set<OffsetDateTime> setOfOffsetDateTimeWithDefault() {
    return Set.of(DEFAULT_OFFSET_DATE_TIME);
  }

}
