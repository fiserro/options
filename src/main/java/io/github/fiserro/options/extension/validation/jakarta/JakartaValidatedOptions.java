package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.extension.OptionsExtensions;

@OptionsExtensions({JakartaValidator.class})
public interface JakartaValidatedOptions<T extends JakartaValidatedOptions<T>> extends Options<T> {

}
