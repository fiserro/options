package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;

public interface Strings {

  @NotNull
  @Pattern(regexp = "^[a-zA-Z0-9]+$")
  @Option
  String string();

  @Option
  default String stringWithDefault() {
    return "default";
  }

  @NotNull
  @Option
  List<String> listOfString();

  @Size(min = 3, max = 5)
  @Option
  default List<String> listOfStringWithDefault() {
    return List.of("a", "b", "c");
  }

  @Size(min = 3, max = 5)
  @Option
  Set<String> setOfString();

  @Size(min = 3, max = 5)
  @Option
  default Set<String> setOfStringWithDefault() {
    return Set.of("a", "b", "c");
  }

}
