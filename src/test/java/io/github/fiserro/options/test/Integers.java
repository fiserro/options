package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import java.util.List;
import java.util.Set;

public interface Integers {

  @Option
  int primitiveInt();

  @Option
  default int primitiveIntWithDefault() {
    return 10;
  }

  @Option
  Integer integer();

  @Option
  default Integer integerWithDefault() {
    return 10;
  }

  @Option
  List<Integer> listOfInt();

  @Option
  default List<Integer> listOfIntWithDefault() {
    return List.of(1, 2, 3);
  }

  @Option
  Set<Integer> setOfInt();

  @Option
  default Set<Integer> setOfIntWithDefault() {
    return Set.of(1, 2, 3);
  }
}
