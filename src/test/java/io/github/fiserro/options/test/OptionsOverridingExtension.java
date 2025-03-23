package io.github.fiserro.options.test;

import io.github.fiserro.options.extension.ArgumentsSpace;
import io.github.fiserro.options.extension.OptionsExtensions;

@OptionsExtensions({ArgumentsSpace.class})
public interface OptionsOverridingExtension extends OptionsAll {

}
