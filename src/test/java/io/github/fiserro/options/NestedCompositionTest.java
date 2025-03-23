package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import io.github.fiserro.options.OptionPath;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.extension.Envio;
import io.github.fiserro.options.test.OptionsNestedComposition;
import io.github.fiserro.options.test.OptionsStrings;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(PER_CLASS)
class NestedCompositionTest {

  @Test
  void pathOfNestedOptions() {
    OptionsNestedComposition options = OptionsFactory.create(OptionsNestedComposition.class);
    options.options().forEach(l1 -> {
      l1.children().forEach(l2 -> {
        assertThat(l2.path(), is(OptionPath.of(l1.name()).add(l2.name())));
      });
    });
  }

  @Test
  void cloneNestedOptions() {
    OptionsNestedComposition options = OptionsFactory
        .create(OptionsNestedComposition.class, "--source.string=text",
            "--target.listOfString=x,y,z");
    OptionsBuilder<OptionsNestedComposition> builder = options.toBuilder();

    builder.setValue("new text", "source", "string");
    options = builder.build();

    assertThat(options.source().string(), is("new text"));
  }

  @Test
  void createNestedFromUnitEnvironmentVariables() {
    Envio.setVar("PROJECT_NAME", "namespace/project");
    Envio.setVar("ENVIRONMENT_NAME", "test");
    Envio.setVar("UNIT_NAME", "unit1");
    Envio.setVar("SOURCE__STRING", "source input");
    Envio.setVar("TARGET__LIST_OF_STRING", "x,y,z");
    OptionsNestedComposition options = OptionsFactory.create(OptionsNestedComposition.class);
    Envio.clear();

    OptionsStrings source = options.source();
    assertThat(source.string(), is("source input"));
    assertThat(source.stringWithDefault(), is("default"));
    assertThat(source.listOfString(), nullValue());
    assertThat(source.listOfStringWithDefault(), is(Arrays.asList("a", "b", "c")));
    assertThat(source.setOfString(), nullValue());
    assertThat(source.setOfStringWithDefault(), is(Set.of("a", "b", "c")));

    OptionsStrings target = options.target();
    assertThat(target.string(), nullValue());
    assertThat(target.stringWithDefault(), is("default"));
    assertThat(target.listOfString(), is(Arrays.asList("x", "y", "z")));
    assertThat(target.listOfStringWithDefault(), is(Arrays.asList("a", "b", "c")));
    assertThat(target.setOfString(), nullValue());
    assertThat(target.setOfStringWithDefault(), is(Set.of("a", "b", "c")));
  }

  @ParameterizedTest
  @MethodSource("arguments")
  void createNestedFromDifferentSources(OptionsNestedComposition options) {

    OptionsStrings source = options.source();
    assertThat(source.string(), is("text"));
    assertThat(source.stringWithDefault(), is("default"));
    assertThat(source.listOfString(), nullValue());
    assertThat(source.listOfStringWithDefault(), is(Arrays.asList("a", "b", "c")));
    assertThat(source.setOfString(), nullValue());
    assertThat(source.setOfStringWithDefault(), is(Set.of("a", "b", "c")));

    OptionsStrings target = options.target();
    assertThat(target.string(), nullValue());
    assertThat(target.stringWithDefault(), is("default"));
    assertThat(target.listOfString(), is(Arrays.asList("x", "y", "z")));
    assertThat(target.listOfStringWithDefault(), is(Arrays.asList("a", "b", "c")));
    assertThat(target.setOfString(), nullValue());
    assertThat(target.setOfStringWithDefault(), is(Set.of("a", "b", "c")));
  }

  Stream<Arguments> arguments() {
    Map<String, Object> values = Map.of(
        "source", Map.of("string", "text"),
        "target", Map.of("listOfString", Arrays.asList("x", "y", "z"))
    );
    var o1 = OptionsFactory.create(OptionsNestedComposition.class, values);

    var o2 = OptionsFactory.create(OptionsNestedComposition.class, "--source.string=text",
        "--target.listOfString=x,y,z");

    Envio.setVar("SOURCE__STRING", "text");
    Envio.setVar("TARGET__LIST_OF_STRING", "x,y,z");
    var o3 = OptionsFactory.create(OptionsNestedComposition.class);
    Envio.clear();

    return Stream.of(o1, o2, o3)
        .map(Arguments::of);
  }
}
