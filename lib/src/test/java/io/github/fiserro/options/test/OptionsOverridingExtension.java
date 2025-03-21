package io.github.fiserro.options.test;

import io.github.fiserro.options.extension.OptionsExtensions;
import io.github.fiserro.options.extension.impl.ArgumentsSpace;

@OptionsExtensions({ArgumentsSpace.class})
public interface OptionsOverridingExtension extends OptionsAll {

}
