package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import java.util.List;

public interface OptionsDependentDefaults extends Options {

  @Option
  int count();

  @Option
  default List<String> strings() {
    return List.of("a", "b", "c");
  }

  @Option
  default int weight() {
    return count() + strings().size();
  }

}
