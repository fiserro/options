package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface OptionsDuplicates extends OptionsIntegers, OptionsStrings, OptionsEnum {

  @Option
  int primitiveInt();

  @Option
  @Override
  default int primitiveIntWithDefault() {
    return 10;
  }

  @NotNull
  @Option
  @Override
  Integer integer();

  @Option
  @Override
  default List<String> listOfStringWithDefault() {
    return List.of("a", "b", "c");
  }

  @Option
  @Override
  default List<TestEnum> listOfEnumWithDefault() {
    return List.of(TestEnum.ONE, TestEnum.TWO, TestEnum.THREE);
  }


}
