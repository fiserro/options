
# Java Options Library

A type-safe configuration library for Java that supports validation using Jakarta Bean Validation.

## Features

| Feature                                       | Status |
|-----------------------------------------------|--------|
| Type-safe configuration through interfaces    | ✅ |
| Default values using default interface method | ✅ |
| Default values for primitive types            | ✅ |
| All common types and orimitives               | ✅ |
| Collections                                   | ✅ |
| Enums                                         | ✅ |
| java.time.* and java.util.Date                | ✅ |
| Inheritance                                   | ✅ |
| Composition of nested options                 | ✅ |
| Composition - Collection of nested options    | ❌ |
| Custom extensions support                     | ✅ |
| Jakarta Validation support                    | ✅ |
| Jakarta Validations - message templates       | ❌ |
| Jakarta Validations - groups                  | ❌ |
| Load from environment variables               | ✅ |
| Load from command line arguments              | ✅ |
| Load from HashMap                             | ✅ |
| Load from properties file                     | ❌ |
| Load from database                            | ❌ |
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

Options interfaces can inherit from multiple interfaces. 
Be aware that only the last interface in the inheritance chain can expose the fluent `Options` interface. 
If some of the inherited interfaces also extend the `Options` interface, the compiler will throw an error.
Imagine that every sub-interface of the `Options` interface is implemented dynamically and extends the AbstractOptions class with fluent API.
The library will throw an exception if you try to create an instance of the options interface that does not extend the `Options` interface.

```java
import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;

public interface DatabaseConfig {

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

public interface CacheConfig {

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
public interface ApplicationConfig extends DatabaseConfig, CacheConfig, Options<ApplicationConfig> {

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

public interface ServerConfig extends JakartaValidatedOptions<ServerConfig> {

  @NotNull
  @Pattern(regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$")
  @Option
  String host();

  @Min(1024)
  @Max(65535)
  @Option
  int port();
}

public interface ServerConfigHA extends Options<ServerConfigHA> {

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

### Adding Custom Extensions
Just create a class that implements the `OptionsExtension` interface and add it to the `@OptionsExtensions` annotation.
You can load values from database or any other source. You can also add custom validation logic.

### Adding bussiness logic to options
You can add methods to your options interfaces. These methods can depend on other options or can be used to calculate values based on other options.
It may be useful when you have a hundreds of microservices and you don't want to duplicate the same logic in each service.

```java
import io.github.fiserro.options.Option;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Application options parent interface.
 */
public interface AppOptions extends Options<AppOptions> {

  enum Environment {
    DEV, TEST, PROD
  }

  @NotNull
  @Option
  Environment environment();

  @NotNull
  @Pattern(regexp = "[a-z]+/[a-z]+")
  @Option
  String applicationName();
  
  default boolean isProduction() {
    return AppOptions.PROD == environment();
  }
  
  default String s3BucketName() {
    return applicationName() + "-" + environment().name().toLowerCase();
  }
}
```

### Changing values at runtime
The instance of the options interface is immutable. If you need to change some values at runtime, you have to create a new instance of the options.
You can use the `toBuilder()` method to create a new instance of the options with the same values as the original instance.
Then you can change the values you need and create a new instance of the options.

```java
  AppOptions config = OptionsFactory.create(AppOptions.class, args)
    .toBuilder()
    .withValue("environment", "test")
    .build();
```

Or you can use withValue method directly on the instance of the options interface.

```java
  AppOptions config = OptionsFactory.create(AppOptions.class, args)
    .withValue("environment", "test");
```

If you don't like using string option names, you can use the `with` methods generated by the library. 
Just add the `with` prefix to the option name and the library will generate the method for you.

```java
import io.github.fiserro.options.Option;

interface AppOptions extends Options {

  @Option
  int threadCount();

  MyOptions withThreadCount(int threadCount);
}

```

Then the method `withThreadCount` will be generated in the implementation of the options interface:

```java
  AppOptions config = OptionsFactory.create(AppOptions.class, args)
    .withThreadCount(50);
```

Keep in mind that the original instance of the options will not be changed. Every modification will create a new instance of the options.

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
Apache License 2.0