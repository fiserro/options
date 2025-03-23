package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.Option;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public interface JakartaValidatedTestOptions extends JakartaValidatedOptions {

  @Size(min = 5, max = 10)
  @Option
  String string();

  @DecimalMax("10")
  @Option
  Integer max10();

  @DecimalMin("10")
  @Option
  Integer min10();

  @NotNull
  @Option
  Boolean bool();

  @Size(min = 1, max = 10)
  @Option
  List<Integer> list();
}
