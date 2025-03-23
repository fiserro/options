package io.github.fiserro.options.extension;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Type of the extension. Order of the enum values is important because it defines the order of the
 * extension application.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public
enum OptionExtensionType {
  LOAD_FROM_FILE(true),
  LOAD_FROM_DB(true),
  LOAD_FROM_ENV(true),
  LOAD_FROM_ARGS(true),
  VALIDATION(false);
  private final boolean exclusive;
}
