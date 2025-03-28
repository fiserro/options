package io.github.fiserro.options.example;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// use this for extending the options
public interface JdbcProperties {

  // use this for composing the options
  interface JdbcOptions extends JdbcProperties, Options<JdbcOptions> {

  }

  @NotNull
  @Option
  String host();

  @Max(65535)
  @Positive
  @Option
  default int port() {
    return 3306;
  }

  @NotNull
  @Option
  Type type();

  @NotNull
  @Option
  String username();

  @NotNull
  @Option
  String password();

  @NotNull
  @Option
  String database();

  default String connectionString() {
    return switch (type()) {
      case MYSQL -> connectionString2();
      case POSTGRESQL -> connectionString3();
      default -> connectionString1();
    };
  }

  default String connectionString1() {
    return String.format("jdbc:%s://%s:%d/%s", type().name, host(), port(), database());
  }

  default String connectionString2() {
    return String.format("jdbc:%s://%s:%d/%s?user=%s&password=%s", type().name, host(), port(),
        database(), username(), password());
  }

  default String connectionString3() {
    return String.format("jdbc:%s://%s/%s", type().name, host(), database());
  }

  enum Type {
    MYSQL("mysql"),
    POSTGRESQL("postgresql"),
    ORACLE("oracle"),
    SQLSERVER("sqlserver"),
    SQLITE("sqlite");

    private final String name;

    Type(String name) {
      this.name = name;
    }
  }

}
