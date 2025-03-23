package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.extension.validation.jakarta.JakartaValidatedOptions;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public interface JakartaValidatedTestOptions extends JakartaValidatedOptions {

  @NotNull
  @Option
  String string();

  @NotNull
  @Option
  Integer integer();

  @NotNull
  @Option
  Boolean bool();

  @Size(min = 1, max = 10)
  @Option
  List<Integer> list();
}
