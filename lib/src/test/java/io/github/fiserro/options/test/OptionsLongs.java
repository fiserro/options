package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import java.util.List;
import java.util.Set;

public interface OptionsLongs extends Options {

  @Option
  long primitiveLong();

  @Option
  default long primitiveLongWithDefault() {
    return 10L;
  }

  @Option
  Long longObject();

  @Option
  default Long longObjectWithDefault() {
    return 10L;
  }

  @Option
  List<Long> listOfLong();

  @Option
  default List<Long> listOfLongWithDefault() {
    return List.of(1L, 2L, 3L);
  }

  @Option
  Set<Long> setOfLong();

  @Option
  default Set<Long> setOfLongWithDefault() {
    return Set.of(1L, 2L, 3L);
  }

}
