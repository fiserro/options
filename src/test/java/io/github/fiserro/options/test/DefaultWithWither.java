package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;

/**
 * Test interface with default method and wither.
 * Used to verify that withers work even when getter has default implementation.
 */
public interface DefaultWithWither extends Options<DefaultWithWither> {

  @Option
  default int value() {
    return 50;
  }

  DefaultWithWither withValue(int value);
}
