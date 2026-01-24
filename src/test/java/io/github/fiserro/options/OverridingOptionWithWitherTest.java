package io.github.fiserro.options;

import io.github.fiserro.options.test.ChildWithOverride;
import io.github.fiserro.options.test.ParentModule.ChildAnnotation;
import io.github.fiserro.options.test.ParentModule.ParentAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for overriding @Option methods with withers defined in parent interface.
 *
 * <p>When a child interface overrides a parent's @Option method:
 * <ul>
 *   <li>The child's annotations should be visible</li>
 *   <li>The parent's wither should still work</li>
 *   <li>The child's default value should be used</li>
 * </ul>
 */
@DisplayName("Overriding @Option with wither in parent")
class OverridingOptionWithWitherTest {

    private ChildWithOverride options;
    private Map<String, OptionDef> optionsByName;

    @BeforeEach
    void setUp() {
        options = OptionsFactory.create(ChildWithOverride.class);
        optionsByName = options.options().stream()
                .collect(Collectors.toMap(OptionDef::name, o -> o));
    }

    @Nested
    @DisplayName("Default values")
    class DefaultValues {

        @Test
        @DisplayName("Child's default value is used")
        void childDefaultValueIsUsed() {
            assertThat(options.power(), is(75));  // Child's default, not parent's 50
        }
    }

    @Nested
    @DisplayName("Wither functionality")
    class WitherFunctionality {

        @Test
        @DisplayName("Parent's wither works on child options")
        void parentWitherWorksOnChildOptions() {
            ChildWithOverride modified = options.withPower(100);
            assertThat(modified.power(), is(100));
        }

        @Test
        @DisplayName("Wither returns correct type")
        void witherReturnsCorrectType() {
            ChildWithOverride modified = options.withPower(100);
            assertNotNull(modified);
            // Can chain withers
            ChildWithOverride chained = modified.withPower(200);
            assertThat(chained.power(), is(200));
        }
    }

    @Nested
    @DisplayName("Annotation resolution")
    class AnnotationResolution {

        @Test
        @DisplayName("Child's annotation is visible via OptionDef")
        void childAnnotationIsVisible() {
            OptionDef powerOpt = optionsByName.get("power");
            assertNotNull(powerOpt, "power option should exist");

            ChildAnnotation childAnnotation = powerOpt.annotations().stream()
                    .filter(ChildAnnotation.class::isInstance)
                    .map(ChildAnnotation.class::cast)
                    .findFirst()
                    .orElse(null);

            assertNotNull(childAnnotation, "Child's @ChildAnnotation should be visible");
            assertThat(childAnnotation.value(), is("from-child"));
        }

        @Test
        @DisplayName("Parent's annotation is NOT visible (replaced by child)")
        void parentAnnotationIsNotVisible() {
            OptionDef powerOpt = optionsByName.get("power");
            assertNotNull(powerOpt, "power option should exist");

            boolean hasParentAnnotation = powerOpt.annotations().stream()
                    .anyMatch(ParentAnnotation.class::isInstance);

            // Parent annotation should NOT be visible - child's method replaces parent's
            assertFalse(hasParentAnnotation, "Parent's @ParentAnnotation should NOT be visible when child overrides with @Option");
        }

        @Test
        @DisplayName("OptionDef.method() returns child's method")
        void optionDefMethodReturnsChildMethod() {
            OptionDef powerOpt = optionsByName.get("power");
            assertNotNull(powerOpt, "power option should exist");

            assertThat(powerOpt.method().getDeclaringClass().getSimpleName(), is("ChildWithOverride"));
        }
    }

    @Nested
    @DisplayName("Option scanning")
    class OptionScanning {

        @Test
        @DisplayName("Only one 'power' option exists (not duplicated)")
        void onlyOnePowerOptionExists() {
            long powerCount = options.options().stream()
                    .filter(o -> o.name().equals("power"))
                    .count();

            assertThat(powerCount, is(1L));
        }

        @Test
        @DisplayName("OptionDef has wither reference")
        void optionDefHasWitherReference() {
            OptionDef powerOpt = optionsByName.get("power");
            assertNotNull(powerOpt, "power option should exist");
            assertNotNull(powerOpt.wither(), "power option should have wither from parent");
        }
    }
}
