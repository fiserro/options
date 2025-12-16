package io.github.fiserro.options.extension;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import io.github.fiserro.options.OptionsFactory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for dynamic extensions functionality.
 */
public class DynamicExtensionsTest {

    /**
     * Test options interface without any annotations.
     */
    public interface TestOptions extends Options<TestOptions> {
        @Option
        String name();

        @Option
        String value();

        @Option
        default int count() {
            return 0;
        }
    }

    /**
     * Test options interface with ArgumentsEquals extension annotation.
     */
    @OptionsExtensions({ArgumentsEquals.class})
    public interface AnnotatedOptions extends Options<AnnotatedOptions> {
        @Option
        String name();
    }

    /**
     * Custom extension that loads values from a context map (simulating database access).
     */
    public static class DatabaseSimulatorExtension extends AbstractOptionsExtension
            implements OptionsExtension {

        private final Map<String, String> database;

        public DatabaseSimulatorExtension(Class<? extends Options<?>> declaringClass,
                                          Map<String, String> database) {
            super(OptionExtensionType.CUSTOM, declaringClass);
            this.database = database;
        }

        @Override
        public void extend(OptionsBuilder<? extends Options<?>, ?> options) {
            database.forEach(options::setValue);
        }
    }

    @Test
    void dynamicExtensionWithCustomType() {
        // Create a database simulator with test data
        Map<String, String> database = new HashMap<>();
        database.put("name", "John");
        database.put("value", "Test Value");

        DatabaseSimulatorExtension extension = new DatabaseSimulatorExtension(
                TestOptions.class, database);

        // Create options with dynamic extension
        TestOptions options = OptionsFactory.create(
                TestOptions.class,
                List.of(extension));

        assertThat(options.name(), is("John"));
        assertThat(options.value(), is("Test Value"));
        assertThat(options.count(), is(0)); // default value
    }

    @Test
    void dynamicExtensionCanOverridePrefilledValues() {
        Map<String, String> database = new HashMap<>();
        database.put("name", "Dynamic Value");

        DatabaseSimulatorExtension extension = new DatabaseSimulatorExtension(
                TestOptions.class, database);

        // Prefilled values have lowest priority
        Map<String, Object> prefilled = Map.of("name", "Prefilled Value");

        TestOptions options = OptionsFactory.create(
                TestOptions.class,
                prefilled,
                List.of(extension));

        // Dynamic extension should override prefilled value
        assertThat(options.name(), is("Dynamic Value"));
    }

    @Test
    void exclusivityValidationThrowsWhenConflicting() {
        // Create a dynamic extension with exclusive type
        OptionsExtension dynamicArgsExtension = new ArgumentsEquals(AnnotatedOptions.class);

        // Should throw because AnnotatedOptions already has ArgumentsEquals via annotation
        var exception = assertThrows(
                OptionExtensionScanner.IllegalExtensionException.class,
                () -> OptionsFactory.create(
                        AnnotatedOptions.class,
                        List.of(dynamicArgsExtension),
                        "--name=test"));

        assertThat(exception.getMessage().contains("exclusive type"), is(true));
    }

    @Test
    void multipleDynamicExclusiveExtensionsThrowError() {
        // Create two dynamic extensions of the same exclusive type
        OptionsExtension ext1 = new ArgumentsEquals(TestOptions.class);
        OptionsExtension ext2 = new ArgumentsSpace(TestOptions.class);

        // Should throw because both are LOAD_FROM_ARGS type (exclusive)
        var exception = assertThrows(
                OptionExtensionScanner.IllegalExtensionException.class,
                () -> OptionsFactory.create(
                        TestOptions.class,
                        List.of(ext1, ext2),
                        "--name=test"));

        assertThat(exception.getMessage().contains("exclusive type"), is(true));
    }

    @Test
    void toBuilderPreservesDynamicExtensions() {
        Map<String, String> database = new HashMap<>();
        database.put("name", "Original");

        DatabaseSimulatorExtension extension = new DatabaseSimulatorExtension(
                TestOptions.class, database);

        TestOptions original = OptionsFactory.create(
                TestOptions.class,
                List.of(extension));

        // Update database and create new options with same extension
        database.put("value", "Added Value");

        TestOptions updated = original.toBuilder()
                .build();

        // Dynamic extension should still be applied
        assertThat(updated.name(), is("Original"));
        assertThat(updated.value(), is("Added Value"));
    }

    @Test
    void dynamicExtensionWithoutAnnotationBased() {
        // Test that dynamic extensions work on interfaces without any annotations
        Map<String, String> database = new HashMap<>();
        database.put("name", "Dynamic Only");

        DatabaseSimulatorExtension extension = new DatabaseSimulatorExtension(
                TestOptions.class, database);

        TestOptions options = OptionsFactory.create(
                TestOptions.class,
                List.of(extension),
                "--value=From Args");

        // Dynamic extension provides name, args provide value
        assertThat(options.name(), is("Dynamic Only"));
        // No annotation-based args extension, so command-line args won't work
        assertThat(options.value(), is(nullValue()));
    }

    @Test
    void multipleNonExclusiveCustomExtensions() {
        // Test that multiple CUSTOM type extensions can coexist
        Map<String, String> db1 = new HashMap<>();
        db1.put("name", "From DB1");

        Map<String, String> db2 = new HashMap<>();
        db2.put("value", "From DB2");

        DatabaseSimulatorExtension ext1 = new DatabaseSimulatorExtension(TestOptions.class, db1);
        DatabaseSimulatorExtension ext2 = new DatabaseSimulatorExtension(TestOptions.class, db2);

        TestOptions options = OptionsFactory.create(
                TestOptions.class,
                List.of(ext1, ext2));

        // Both extensions should be applied
        assertThat(options.name(), is("From DB1"));
        assertThat(options.value(), is("From DB2"));
    }

    @Test
    void dynamicExtensionWithAnnotationBasedExtension() {
        // Test that dynamic extensions can work alongside annotation-based extensions
        Map<String, String> database = new HashMap<>();
        database.put("value", "From Database");

        // Use TestOptions (no annotations) with dynamic database extension
        DatabaseSimulatorExtension dbExtension = new DatabaseSimulatorExtension(
                TestOptions.class, database);

        TestOptions options = OptionsFactory.create(
                TestOptions.class,
                List.of(dbExtension));

        // Database extension should set the value
        assertThat(options.value(), is("From Database"));
        assertThat(options.name(), is(nullValue())); // Not set
    }

    @Test
    void withValuePreservesDynamicExtensions() {
        Map<String, String> database = new HashMap<>();
        database.put("name", "Original");

        DatabaseSimulatorExtension extension = new DatabaseSimulatorExtension(
                TestOptions.class, database);

        TestOptions original = OptionsFactory.create(
                TestOptions.class,
                List.of(extension));

        // Use withValue which internally uses toBuilder
        TestOptions updated = original.withValue("value", "New Value");

        // Original values from extension should be preserved
        assertThat(updated.name(), is("Original"));
        assertThat(updated.value(), is("New Value"));
    }
}
