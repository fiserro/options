package io.github.fiserro.options.test;

import io.github.fiserro.options.Options;

public interface DefaultMethodOptions extends DateTime, DependentDefaults, Enum, Integers, Longs,
    Strings, Options<DefaultMethodOptions> {

  record AppInfo(Integer integer, String string, TestEnum enumeration) {

  }

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
