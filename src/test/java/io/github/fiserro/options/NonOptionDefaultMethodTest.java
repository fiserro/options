package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.test.OptionsAll;
import io.github.fiserro.options.test.OptionsEnum.TestEnum;
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
    OptionsWithDefaultMethod options = OptionsFactory.create(OptionsWithDefaultMethod.class,
        values);
    assertThat(options.appInfo(), is(new AppInfo(6, "unit", TestEnum.ONE)));
    assertThat(options.appInfo(5), is(new AppInfo(5, "unit", TestEnum.ONE)));
  }

  public interface OptionsWithDefaultMethod extends OptionsAll {

    /**
     * To test the default method.
     *
     * @return the app info
     */
    default AppInfo appInfo() {
      return new AppInfo(integer(), string(), enumValue());
    }

    /**
     * To test the default method with parameters.
     *
     * @param param some parameter
     * @return the app info
     */
    default AppInfo appInfo(int param) {
      return new AppInfo(param, string(), enumValue());
    }
  }

  public record AppInfo(Integer integer, String string, TestEnum enumeration) {

  }
}
