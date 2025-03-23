package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import java.util.List;
import java.util.Set;

public interface OptionsEnum extends Options {

  enum TestEnum {
    ONE, TWO, THREE
  }

  @Option
  TestEnum enumValue();

  @Option
  default TestEnum enumValueWithDefault() {
    return TestEnum.ONE;
  }

  @Option
  List<TestEnum> listOfEnum();

  @Option
  default List<TestEnum> listOfEnumWithDefault() {
    return List.of(TestEnum.ONE, TestEnum.TWO, TestEnum.THREE);
  }

  @Option
  Set<TestEnum> setOfEnum();

  @Option
  default Set<TestEnum> setOfEnumWithDefault() {
    return Set.of(TestEnum.ONE, TestEnum.TWO, TestEnum.THREE);
  }

}
