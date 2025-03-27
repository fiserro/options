package io.github.fiserro.options.extension;

import static io.github.fiserro.options.extension.OptionExtensionType.LOAD_FROM_ARGS;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.extension.OptionExtensionScanner.IllegalExtensionException;
import io.github.fiserro.options.test.OverridingExtensionOptions;
import java.util.List;
import java.util.NavigableMap;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class OptionExtensionScannerTest {

  private final OptionExtensionScanner scanner = new OptionExtensionScanner();

  @Test
  void scanExtensions() {
    NavigableMap<OptionExtensionType, List<OptionsExtension>> extensions = scanner.scan(
        OptionsValid.class);
    MatcherAssert.assertThat(extensions.size(), is(2));
  }

  @Test
  void scanExtensionsOverride() {
    NavigableMap<OptionExtensionType, List<OptionsExtension>> extensions = scanner.scan(
        OverridingExtensionOptions.class);
    MatcherAssert.assertThat(extensions.get(LOAD_FROM_ARGS).size(), is(1));
    MatcherAssert.assertThat(extensions.get(LOAD_FROM_ARGS).getFirst(),
        instanceOf(ArgumentsSpace.class));
  }

  @Test
  void scanExtensionsInvalid() {
    IllegalExtensionException exception = assertThrows(IllegalExtensionException.class,
        () -> scanner.scan(OptionsInvalid.class));
    assertEquals("OptionsInvalid declares more than one (2) exclusive extension of the same type " +
        LOAD_FROM_ARGS, exception.getMessage());
  }

  @Test
  void scanInheritedExtensions() {
    NavigableMap<OptionExtensionType, List<OptionsExtension>> extensions = scanner.scan(
        OptionsInherited.class);
    MatcherAssert.assertThat(extensions.size(), is(2));
  }

  public interface OptionsInherited extends OptionsValid {

  }

  @OptionsExtensions({ArgumentsEquals.class, EnvironmentVariables.class})
  public interface OptionsValid extends Options {

  }


  @OptionsExtensions({ArgumentsEquals.class, ArgumentsSpace.class})
  public interface OptionsInvalid extends Options {

  }
}