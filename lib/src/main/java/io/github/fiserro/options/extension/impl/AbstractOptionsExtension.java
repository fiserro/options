package io.github.fiserro.options.extension.impl;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.extension.OptionExtensionType;
import io.github.fiserro.options.extension.OptionsExtension;
import lombok.RequiredArgsConstructor;

/**
 * Abstract implementation of {@link OptionsExtension}.
 */
@RequiredArgsConstructor
public abstract class AbstractOptionsExtension implements OptionsExtension {

  private final OptionExtensionType type;
  private final Class<? extends Options> declaringClass;

  @Override
  public OptionExtensionType type() {
    return type;
  }

  @Override
  public Class<? extends Options> declaringClass() {
    return declaringClass;
  }
}
