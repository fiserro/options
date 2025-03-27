package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.test.DefaultMethodOptions;
import io.github.fiserro.options.test.DefaultMethodOptions.AppInfo;
import io.github.fiserro.options.test.Enum.TestEnum;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * When somebody possibly break the default method delegation in {@link OptionsFactory} this test
 * will fail. It also tests that any object can be returned from default methods.
 */
class NonOptionDefaultMethodTest {

  @Test
  void notOptionDefaultMethodWorks() {
    Map<String, Object> values = Map.of(
        "integer", 6,
        "string", "unit",
        "enumValue", TestEnum.ONE
    );
    DefaultMethodOptions options = OptionsFactory.create(DefaultMethodOptions.class,
        values);
    assertThat(options.appInfo(), is(new AppInfo(6, "unit", TestEnum.ONE)));
    assertThat(options.appInfo(5), is(new AppInfo(5, "unit", TestEnum.ONE)));
  }
}
