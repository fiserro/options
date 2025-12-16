# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java type-safe configuration library that dynamically generates configuration classes from interfaces using ByteBuddy. It supports loading configuration from multiple sources (environment variables, command-line arguments, programmatic maps) and provides Jakarta Bean Validation integration.

## Build and Test Commands

### Maven Commands
- `mvn clean package` - Build the project
- `mvn test` - Run all tests
- `mvn test -Dtest=ClassName` - Run a specific test class
- `mvn test -Dtest=ClassName#methodName` - Run a single test method
- `mvn clean install` - Install to local Maven repository

### Requirements
- Java 21 or later
- Maven

## Core Architecture

### Dynamic Proxy Generation Pattern

The library uses ByteBuddy to dynamically generate implementations of user-defined options interfaces at runtime:

1. **User Interface Definition**: Users define configuration as an interface extending `Options<T>` with methods annotated with `@Option`
2. **Scanning Phase**: `OptionScanner` analyzes the interface to extract option definitions (`OptionDef`)
3. **Builder Phase**: `OptionsBuilder` accumulates values from various sources (extensions load from env vars, args, etc.)
4. **Generation Phase**: `OptionsFactory` uses ByteBuddy to create a class that extends `AbstractOptions` and implements the user's interface
5. **Method Interception**: ByteBuddy intercepts method calls to option methods and delegates to `GetValueInterceptor` and `WithValueInterceptor`

### Key Components

**OptionsFactory** (`src/main/java/io/github/fiserro/options/OptionsFactory.java`)
- Entry point for creating options instances
- Coordinates extension application and dynamic class generation
- Uses ByteBuddy to generate implementation classes at runtime
- Intercepts option getter methods with `GetValueInterceptor`
- Intercepts `withX()` methods with `WithValueInterceptor` for immutable value updates

**OptionsBuilder** (`src/main/java/io/github/fiserro/options/OptionsBuilder.java`)
- Mutable builder for accumulating option values before generating the immutable Options instance
- Extensions modify the builder to load values from various sources
- Handles nested options (composition) by creating child builders
- Provides `setValue()`/`getValue()` methods and fluent `withValue()` API
- Deep copies values to ensure immutability

**AbstractOptions** (`src/main/java/io/github/fiserro/options/AbstractOptions.java`)
- Base class for all dynamically generated options implementations
- Stores option values in an immutable map
- Implements core Options interface methods: `getValue()`, `withValue()`, `toBuilder()`, `validate()`
- Generated classes extend this and add intercepted option methods

**OptionScanner** (`src/main/java/io/github/fiserro/options/OptionScanner.java`)
- Scans interface hierarchy to discover all option methods
- Creates `OptionDef` objects describing each option's metadata
- Detects nested options (composition) and recursively scans them

**OptionDef** (`src/main/java/io/github/fiserro/options/OptionDef.java`)
- Metadata about a single option: name, type, default value, validation annotations, etc.
- Provides option keys for different sources (Java name, env var name, aliases)
- Handles primitive type defaults

### Extension System

Extensions are applied during the build phase to load values from different sources. The extension lifecycle:

1. **Registration**: Extensions can be registered two ways:
   - **Static**: Via `@OptionsExtensions({ExtensionClass.class})` annotation on the interface
   - **Dynamic**: Passed as `List<OptionsExtension>` parameter to `OptionsFactory.create()`
2. **Scanning**: `OptionExtensionScanner` discovers annotation-based extensions from the interface hierarchy
3. **Merging**: Dynamic extensions are merged with annotation-based extensions and validated for exclusivity
4. **Prioritization**: Extensions are sorted by `OptionExtensionType` priority in reverse order
5. **Application**: Each extension's `extend(OptionsBuilder)` method is called to populate values
6. **Validation**: Validation extensions run separately after the instance is created

**Extension Priority (reverse order application)**:
- `LOAD_FROM_FILE` (highest priority - applied last)
- `LOAD_FROM_DB`
- `LOAD_FROM_ENV`
- `LOAD_FROM_ARGS`
- `CUSTOM` (lowest priority - applied first, allows others to override)
- `VALIDATION` (special - only runs during validation, not during build)

**OptionsExtension Interface** (`src/main/java/io/github/fiserro/options/extension/OptionsExtension.java`)
- All extensions implement this interface
- `extend(OptionsBuilder)` - called to populate values
- `type()` - returns extension type/priority
- `declaringClass()` - returns the options interface class
- Built-in extensions in `src/main/java/io/github/fiserro/options/extension/`:
  - `EnvironmentVariables` - loads from environment variables (converts to UPPER_SNAKE_CASE)
  - `ArgumentsEquals` - parses `--key=value` arguments
  - `ArgumentsSpace` - parses `--key value` arguments
  - `JakartaValidator` - validates using Jakarta Bean Validation annotations

**Dynamic Extensions**:
Dynamic extensions allow passing extension instances with runtime context (database connections, etc.) to factory methods:

```java
// Custom extension with database context
public class DatabaseExtension extends AbstractOptionsExtension {
    private final Database db;

    public DatabaseExtension(Class<? extends Options<?>> declaringClass, Database db) {
        super(OptionExtensionType.CUSTOM, declaringClass);
        this.db = db;
    }

    @Override
    public void extend(OptionsBuilder<?, ?> builder) {
        // Load values from database
        db.getConfigs().forEach(builder::setValue);
    }
}

// Use dynamic extension
Database db = new Database();
MyOptions options = OptionsFactory.create(
    MyOptions.class,
    List.of(new DatabaseExtension(MyOptions.class, db)),
    args);
```

**Exclusivity Rules**:
- Exclusive types (`LOAD_FROM_FILE`, `LOAD_FROM_DB`, `LOAD_FROM_ENV`, `LOAD_FROM_ARGS`) can only have one extension per type
- If an annotation-based exclusive extension exists, adding a dynamic extension of the same type throws `IllegalExtensionException`
- Multiple dynamic extensions of the same exclusive type throw `IllegalExtensionException`
- Non-exclusive types (`CUSTOM`, `VALIDATION`) can have multiple extensions

**toBuilder() Preservation**:
Dynamic extensions are preserved when using `toBuilder()` or `withValue()`, ensuring consistent behavior throughout the options lifecycle

### Nested Options (Composition)

Options interfaces can contain nested options as properties. This is handled specially:

1. The `OptionScanner` detects when an option method returns a type that extends `Options`
2. A child `OptionsBuilder` is created for the nested option
3. Extensions populate nested values using dot notation: `--parent.child.key=value` or `PARENT_CHILD_KEY=env_value`
4. During generation, nested builders are recursively built into nested options instances

### Immutability Pattern

- Options instances are immutable once created
- `withValue()` returns a new instance with the changed value
- `toBuilder()` creates a new builder from existing options for bulk modifications
- Values are deep-copied in builders to prevent external mutation

## Testing Patterns

Tests are in `src/test/java/io/github/fiserro/options/`:
- Test option interfaces are in `src/test/java/io/github/fiserro/options/test/`
- Tests typically create options with `OptionsFactory.create()` and verify behavior
- Validation tests check constraint violation messages and paths

## Code Style Notes

- Uses Lombok annotations (`@SneakyThrows`, `@RequiredArgsConstructor`, `@EqualsAndHashCode`)
- Prefer immutable collections (`Set.copyOf()`, `List.of()`)
- Use Java 21 features (records, pattern matching, sealed classes where appropriate)
- Exceptions use descriptive messages with context (option names, paths, values)
