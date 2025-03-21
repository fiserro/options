package io.github.fiserro.options.test;

import io.github.fiserro.options.extension.OptionsExtensions;
import io.github.fiserro.options.extension.impl.ArgumentsEquals;
import io.github.fiserro.options.extension.impl.EnvironmentVariables;

@OptionsExtensions({ArgumentsEquals.class, EnvironmentVariables.class})
public interface OptionsAll extends OptionsDateTime, OptionsDependentDefaults, OptionsEnum,
    OptionsIntegers, OptionsLongs, OptionsStrings {

}
