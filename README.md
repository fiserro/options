Here's a generated `README.md` for the project:

```markdown
# Java Options Library

A type-safe configuration library for Java that supports validation using Jakarta Bean Validation.

## Features

- Type-safe configuration through Java interfaces
- Support for Jakarta Bean Validation annotations
- Flexible option value resolution
- Default values for primitive types
- Extensible validation framework

## Usage

### Basic Configuration

Define your configuration interface:

```java
public interface MyConfig extends Options {
    @NotNull
    Boolean getEnabled();
    
    @Min(10)
    @Max(100)
    int getThreadCount();
    
    @NotEmpty
    String getServiceUrl();
}
```

Create configuration instance:

```java
MyConfig config = OptionsFactory.create(MyConfig.class, Map.of(
    "enabled", true,
    "threadCount", 50,
    "serviceUrl", "http://example.com"
));
```

### Supported Validations

The library supports these Jakarta Bean Validation annotations:

- `@NotNull` - Validates that a value is not null
- `@Size` - Validates size/length of arrays, collections, maps, and strings
- `@DecimalMax`, `@DecimalMin` - Validates maximum/minimum decimal values
- `@Min`, `@Max` - Validates maximum/minimum integer values
- `@Positive`, `@PositiveOrZero` - Validates positive numbers
- `@Negative`, `@NegativeOrZero` - Validates negative numbers
- `@Future`, `@FutureOrPresent` - Validates dates/times in the future
- `@Past`, `@PastOrPresent` - Validates dates/times in the past
- `@Digits` - Validates decimal digit constraints
- `@NotBlank` - Validates string is not blank
- `@NotEmpty` - Validates collections/arrays/maps/strings are not empty
- `@Email` - Validates email format
- `@Pattern` - Validates string pattern matching

## Requirements

- Java 17 or later
- Jakarta Bean Validation API

## Installation

Add the dependency to your Maven project:

```xml
<dependency>
    <groupId>io.github.fiserro</groupId>
    <artifactId>options</artifactId>
    <version>[VERSION]</version>
</dependency>
```

## License

[Add your license information here]
```