
# Java Options Library

A type-safe configuration library for Java that supports validation using Jakarta Bean Validation.

## Features

| Feature                                       | Status |
|-----------------------------------------------|--------|
| Type-safe configuration through interfaces    | ✅ |
| Default values using default interface method | ✅ |
| Default values for primitive types            | ✅ |
| All common types and orimitives               | ✅ |
| Collections and Maps                          | ✅ |
| Enums                                         | ✅ |
| java.time.* and java.util.Date                | ✅ |
| Inheritance                                   | ✅ |
| Composition of nested options                 | ✅ |
| Custom extensions support                     | ✅ |
| Jakarta Bean Validation support               | ✅ |
| Load from environment variables               | ✅ |
| Load from command line arguments              | ✅ |
| Load from HashMap                             | ✅ |
| Load from properties file                     | ❌ |
| Load from .env file                           | ❌ |
| Load from JSON file                           | ❌ |
| Load from XML file                            | ❌ |

## Usage

### Basic Configuration

Define your configuration interface:

```java
import io.github.fiserro.options.Option;

@OptionsExtensions({EnvironmentVariables.class, ArgumentsEquals.class})
public interface MyConfig extends Options {

  // default primitive value: false
  @Option
  boolean enabled();

  // default interface method
  @Option
  default int threadCount() {
    return 2;
  }

  // no default value
  @Option
  String serviceUrl();
  
  // you can add options which are not configurable from the outside
  default long timeoutMillis() {
    return 1000;
  }
  
  // they can depend on other options
  default failOverTimeMillis() {
    return getTimeoutMillis() / threadCount();
  }
}
```

Create configuration instance from environment variables:
Expected environment variables: `ENABLED=true`, `THREAD_COUNT=50`, `SERVICE_URL=http://example.com`
To be able to load configuration from environment variables, you need to add `EnvironmentVariables` extension to the `@OptionsExtensions` annotation.
```java
MyConfig config = OptionsFactory.create(MyConfig.class);
```


Create configuration instance from command line arguments:
Expected command line arguments: `--enabled=true`, `--threadCount=50`, `--serviceUrl=http://example.com`
To be able to load configuration from command line arguments, you need to add `ArgumentsEquals` or `ArgumentsSpace` class extension to the `@OptionsExtensions` annotation.
```java
public static void main(String[] args) {
    MyConfig config = OptionsFactory.create(MyConfig.class, args);
}
```

For testing purposes or when you need to load configuration programmatically, you can create configuration instance from HashMap:
```java
MyConfig config = OptionsFactory.create(MyConfig.class, Map.of(
    "enabled", true,
    "threadCount", 50,
    "serviceUrl", "http://example.com"
));
```



### Supported Jakarta Validations

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

To use Jakarta Bean Validation, extend your configuration interface from `JakartaValidatedOptions` or add @OptionsExtensions({JakartaValidator.class}) annotation to your configuration interface.

```java
import io.github.fiserro.options.Option;

public interface MyConfig extends JakartaValidatedOptions {

  @Pattern(regexp = "dev|test|prod")
  @Option
  String environment();
}
```

### Inheritance

Options interfaces can inherit from multiple interfaces. Here's an example:

```java
import io.github.fiserro.options.Option;

public interface DatabaseConfig extends JakartaValidatedOptions {

  @Option
  @NotEmpty
  String url();

  @Option
  @Min(1)
  @Max(100)
  int maxConnections();

  @Option
  @NotNull
  String username();

  @Option
  @NotNull
  String password();
}

public interface CacheConfig extends JakartaValidatedOptions {

  @Option
  @NotNull
  String cacheType();

  @Option
  @Min(60)
  int ttlSeconds();

  @Option
  @PositiveOrZero
  long maxSize();
}

// Inherits all methods from DatabaseConfig and CacheConfig
public interface ApplicationConfig extends DatabaseConfig, CacheConfig {

  @NotEmpty
  String applicationName();

  @Pattern(regexp = "dev|test|prod")
  String environment();


}
```

Create the configuration:
```java
ApplicationConfig config = OptionsFactory.create(ApplicationConfig.class, Map.of(
    "url", "jdbc:postgresql://localhost:5432/mydb",
    "maxConnections", 50,
    "username", "admin",
    "password", "secret",
    "cacheType", "redis",
    "ttlSeconds", 300,
    "maxSize", 1000,
    "applicationName", "myapp",
    "environment", "prod"
));
```


### Composition

Options interfaces can be composed using nested configurations. Here's an example:

```java
import io.github.fiserro.options.Options;

public interface ServerConfig extends JakartaValidatedOptions {

  @NotNull
  @Pattern(regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$")
  @Option
  String host();

  @Min(1024)
  @Max(65535)
  @Option
  int port();
}

public interface ServerConfigHA extends Options {

  @Option
  ServerConfig primaryNode();

  @Option
  default ServerConfig backupNode() {
    return OptionsFactory.create(ServerConfig.class, Map.of(
        "host", "192.168.1.2",
        "port", 8080
    ));
  }
}
```
Create HA server configuration from command line arguments: `--primaryNode.host=192.168.1.1`, `--primaryNode.port=8080`, `--backupNode.host=192.168.1.2`, `--backupNode.port=8081`

```java
public static void main(String[] args) {
    MyConfig config = OptionsFactory.create(MyConfig.class, args);
}
```


## Requirements

- Java 21 or later


## Installation

Add the dependency to your Maven project:

```xml
<dependency>
    <groupId>io.github.fiserro</groupId>
    <artifactId>options</artifactId>
    <version>0.0.1</version>
</dependency>
```

## License

[Add your license information here]
```