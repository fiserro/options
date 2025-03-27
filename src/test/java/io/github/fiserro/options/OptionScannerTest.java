package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.test.AllOptions;
import io.github.fiserro.options.test.DuplicatesOptions;
import io.github.fiserro.options.test.StringsOptionsWith;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OptionScannerTest {

  private final OptionScanner scanner = new OptionScanner();

  @Test
  void scanOwnOptions() {
    Map<String, OptionDef> options = scanner.scanByKeys(AllOptions.class);
    assertThat(options.size(), is(98));
    assertThat(options.get("DATE").description(), is("Date option"));
//    assertThat(options.get("DATE").required(), is(false)); // TODO
    assertThat(options.get("localDate").hasDefaultValue(), is(false));
  }

  @Test
  void scanChildOptions() {
    Map<String, OptionDef> options = scanner.scanByName(DuplicatesOptions.class);
    assertThat(options.size(), is(20));
    assertThat("Overridden option added default value",
        options.get("primitiveIntWithDefault").hasDefaultValue(), is(true));
//    assertThat("Overridden option changed required status", options.get("integer").required(),
//        is(true)); // TODO
    assertThat("Inherited option ", options.get("setOfStringWithDefault").hasDefaultValue(),
        is(true));
  }

  @Test
  void witherMethodScanned() {
    Map<String, OptionDef> options = scanner.scanByName(StringsOptionsWith.class);
    assertThat("Wither method scanned", options.get("string").hasWither(), is(true));
  }

}