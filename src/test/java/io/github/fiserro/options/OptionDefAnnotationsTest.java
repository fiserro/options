package io.github.fiserro.options;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.github.fiserro.options.test.DuplicatesOptions;
import io.github.fiserro.options.test.Integers;
import io.github.fiserro.options.test.Strings;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Tests for the annotations field in OptionDef to verify that annotations are collected from option
 * methods and properly merged when inherited.
 */
class OptionDefAnnotationsTest {

  private final OptionScanner scanner = new OptionScanner();

  private Map<String, OptionDef> toMapByName(Set<OptionDef> options) {
    return options.stream().collect(Collectors.toMap(OptionDef::name, o -> o));
  }

  @Test
  void annotationsFieldIsPopulated() {
    Map<String, OptionDef> options = toMapByName(scanner.scan(Strings.class));

    OptionDef stringOption = options.get("string");
    assertThat("Annotations field should not be null", stringOption.annotations(),
        is(notNullValue()));
    assertThat("Annotations field should not be empty", stringOption.annotations(),
        hasSize(greaterThan(0)));
  }

  @Test
  void annotationsIncludeOptionAnnotation() {
    Map<String, OptionDef> options = toMapByName(scanner.scan(Integers.class));

    OptionDef intOption = options.get("primitiveInt");
    Set<Class<? extends Annotation>> annotationTypes = intOption.annotations().stream()
        .map(Annotation::annotationType)
        .collect(Collectors.toSet());

    assertThat("Annotations should include @Option", annotationTypes, hasItem(Option.class));
  }

  @Test
  void annotationsIncludeAllDecorativeAnnotations() {
    Map<String, OptionDef> options = toMapByName(scanner.scan(Strings.class));

    // string() method has @NotNull, @Pattern, and @Option
    OptionDef stringOption = options.get("string");
    Set<Class<? extends Annotation>> annotationTypes = stringOption.annotations().stream()
        .map(Annotation::annotationType)
        .collect(Collectors.toSet());

    assertThat("Annotations should include @NotNull", annotationTypes, hasItem(NotNull.class));
    assertThat("Annotations should include @Pattern", annotationTypes, hasItem(Pattern.class));
    assertThat("Annotations should include @Option", annotationTypes, hasItem(Option.class));
    assertThat("Should have exactly 3 annotation types", annotationTypes, hasSize(3));
  }

  @Test
  void annotationsIncludeSizeConstraint() {
    Map<String, OptionDef> options = toMapByName(scanner.scan(Strings.class));

    // setOfString() method has @Size and @Option
    OptionDef setOption = options.get("setOfString");
    Set<Class<? extends Annotation>> annotationTypes = setOption.annotations().stream()
        .map(Annotation::annotationType)
        .collect(Collectors.toSet());

    assertThat("Annotations should include @Size", annotationTypes, hasItem(Size.class));
    assertThat("Annotations should include @Option", annotationTypes, hasItem(Option.class));
  }

  @Test
  void annotationsAreMergedInInheritance() {
    Map<String, OptionDef> options = scanner.scanByName(DuplicatesOptions.class);

    // integer() is defined in Integers with @Option, overridden in DuplicatesOptions with
    // @NotNull and @Option
    OptionDef integerOption = options.get("integer");
    Set<Class<? extends Annotation>> annotationTypes = integerOption.annotations().stream()
        .map(Annotation::annotationType)
        .collect(Collectors.toSet());

    assertThat("Merged annotations should include @NotNull", annotationTypes,
        hasItem(NotNull.class));
    assertThat("Merged annotations should include @Option", annotationTypes, hasItem(Option.class));
  }

  @Test
  void annotationsFromOverriddenMethodsAreCombined() {
    Map<String, OptionDef> options = scanner.scanByName(DuplicatesOptions.class);

    // listOfStringWithDefault() is defined in Strings with @Size and @Option,
    // overridden in DuplicatesOptions with just @Option
    // The merged result should have annotations from the override (DuplicatesOptions takes
    // priority)
    OptionDef listOption = options.get("listOfStringWithDefault");

    assertThat("Annotations should not be null", listOption.annotations(), is(notNullValue()));
    assertThat("Should have at least @Option annotation", listOption.annotations(),
        hasSize(greaterThan(0)));

    Set<Class<? extends Annotation>> annotationTypes = listOption.annotations().stream()
        .map(Annotation::annotationType)
        .collect(Collectors.toSet());

    assertThat("Should include @Option", annotationTypes, hasItem(Option.class));
  }

  @Test
  void annotationsCanBeAccessedByType() {
    Map<String, OptionDef> options = toMapByName(scanner.scan(Strings.class));

    OptionDef stringOption = options.get("string");

    // Find the Pattern annotation
    Pattern patternAnnotation = stringOption.annotations().stream()
        .filter(a -> a instanceof Pattern)
        .map(a -> (Pattern) a)
        .findFirst()
        .orElse(null);

    assertThat("Should find Pattern annotation", patternAnnotation, is(notNullValue()));
    assertThat("Pattern should have the correct regexp", patternAnnotation.regexp(),
        is("^[a-zA-Z0-9]+$"));
  }

  @Test
  void annotationsCanBeFilteredByType() {
    Map<String, OptionDef> options = toMapByName(scanner.scan(Strings.class));

    OptionDef setOption = options.get("setOfString");

    // Find the Size annotation
    Size sizeAnnotation = setOption.annotations().stream()
        .filter(a -> a instanceof Size)
        .map(a -> (Size) a)
        .findFirst()
        .orElse(null);

    assertThat("Should find Size annotation", sizeAnnotation, is(notNullValue()));
    assertThat("Size should have min=3", sizeAnnotation.min(), is(3));
    assertThat("Size should have max=5", sizeAnnotation.max(), is(5));
  }

  @Test
  void optionsWithOnlyOptionAnnotationHaveOneAnnotation() {
    Map<String, OptionDef> options = toMapByName(scanner.scan(Integers.class));

    OptionDef intOption = options.get("primitiveInt");

    // This option has only @Option annotation
    assertThat("Should have exactly 1 annotation (@Option)", intOption.annotations(), hasSize(1));

    Set<Class<? extends Annotation>> annotationTypes = intOption.annotations().stream()
        .map(Annotation::annotationType)
        .collect(Collectors.toSet());

    assertThat("Should only contain @Option", annotationTypes,
        containsInAnyOrder(Option.class));
  }
}
