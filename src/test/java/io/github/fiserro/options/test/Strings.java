package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import java.util.List;
import java.util.Set;

public interface Strings {

  @Option
  String string();

  @Option
  default String stringWithDefault() {
    return "default";
  }

  @Option
  List<String> listOfString();

  @Option
  default List<String> listOfStringWithDefault() {
    return List.of("a", "b", "c");
  }

  @Option
  Set<String> setOfString();

  @Option
  default Set<String> setOfStringWithDefault() {
    return Set.of("a", "b", "c");
  }

}
