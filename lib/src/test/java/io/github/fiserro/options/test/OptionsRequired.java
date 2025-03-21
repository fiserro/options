package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.extension.OptionsExtensions;
import io.github.fiserro.options.extension.impl.ValidateRequired;

@OptionsExtensions({ValidateRequired.class})
public interface OptionsRequired extends Options {

  @Option(required = true)
  String requiredString();

  @Option(required = true)
  Integer requiredInteger();

  @Option(required = true)
  Boolean requiredBoolean();
}
