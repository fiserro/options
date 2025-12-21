package io.github.fiserro.options.extension;

import lombok.RequiredArgsConstructor;

/**
 * Abstract implementation of {@link OptionsExtension}.
 */
@RequiredArgsConstructor
public abstract class AbstractOptionsExtension implements OptionsExtension {

  private final OptionExtensionType type;

  @Override
  public OptionExtensionType type() {
    return type;
  }
}
