package io.github.fiserro.options.extension.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsFactory;
import io.github.fiserro.options.extension.EnvironmentFile;
import io.github.fiserro.options.extension.Envio;
import io.github.fiserro.options.extension.OptionsExtensions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EnvironmentFileTest {

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    Envio.clear();
  }

  @AfterEach
  void tearDown() {
    Envio.clear();
  }

  @Test
  void optionsAreLoadedFromEnvFile() throws IOException {
    Path envFile = tempDir.resolve(".env");
    Files.writeString(envFile, """
        STRING_VALUE=hello
        INT_VALUE=42
        """);

    // Change to temp dir for test
    String originalDir = System.getProperty("user.dir");
    try {
      System.setProperty("user.dir", tempDir.toString());

      // Use --envFile to specify the path since we can't change cwd
      EnvFileOptions options = OptionsFactory.create(EnvFileOptions.class,
          "--envFile=" + envFile);

      assertThat(options.stringValue(), is("hello"));
      assertThat(options.intValue(), is(42));
    } finally {
      System.setProperty("user.dir", originalDir);
    }
  }

  @Test
  void envFilePathCanBeOverriddenByArgument() throws IOException {
    Path customEnvFile = tempDir.resolve("custom.env");
    Files.writeString(customEnvFile, """
        STRING_VALUE=from_custom
        INT_VALUE=99
        """);

    EnvFileOptions options = OptionsFactory.create(EnvFileOptions.class,
        "--envFile=" + customEnvFile.toString());

    assertThat(options.stringValue(), is("from_custom"));
    assertThat(options.intValue(), is(99));
  }

  @Test
  void envFilePathCanBeOverriddenByEnvironmentVariable() throws IOException {
    Path customEnvFile = tempDir.resolve("env-var-specified.env");
    Files.writeString(customEnvFile, """
        STRING_VALUE=from_env_var
        INT_VALUE=77
        """);

    Envio.setVar("ENV_FILE", customEnvFile.toString());

    EnvFileOptions options = OptionsFactory.create(EnvFileOptions.class);

    assertThat(options.stringValue(), is("from_env_var"));
    assertThat(options.intValue(), is(77));
  }

  @Test
  void argumentTakesPrecedenceOverEnvironmentVariable() throws IOException {
    Path argFile = tempDir.resolve("arg.env");
    Files.writeString(argFile, """
        STRING_VALUE=from_arg
        INT_VALUE=111
        """);

    Path envVarFile = tempDir.resolve("envvar.env");
    Files.writeString(envVarFile, """
        STRING_VALUE=from_env_var
        INT_VALUE=222
        """);

    Envio.setVar("ENV_FILE", envVarFile.toString());

    EnvFileOptions options = OptionsFactory.create(EnvFileOptions.class,
        "--envFile=" + argFile);

    assertThat(options.stringValue(), is("from_arg"));
    assertThat(options.intValue(), is(111));
  }

  @Test
  void existingEnvVarsTakePrecedenceOverFileValues() throws IOException {
    Path envFile = tempDir.resolve("test.env");
    Files.writeString(envFile, """
        STRING_VALUE=from_file
        INT_VALUE=10
        """);

    // Set an existing env var - this should take precedence
    Envio.setVar("STRING_VALUE", "from_existing_env");

    EnvFileOptions options = OptionsFactory.create(EnvFileOptions.class,
        "--envFile=" + envFile);

    assertThat(options.stringValue(), is("from_existing_env"));
    assertThat(options.intValue(), is(10));
  }

  @Test
  void commentsAndBlankLinesAreIgnored() throws IOException {
    Path envFile = tempDir.resolve("comments.env");
    Files.writeString(envFile, """
        # This is a comment
        STRING_VALUE=hello

        # Another comment
        INT_VALUE=42

        """);

    EnvFileOptions options = OptionsFactory.create(EnvFileOptions.class,
        "--envFile=" + envFile);

    assertThat(options.stringValue(), is("hello"));
    assertThat(options.intValue(), is(42));
  }

  @Test
  void valuesWithEqualsSignAreHandledCorrectly() throws IOException {
    Path envFile = tempDir.resolve("equals.env");
    Files.writeString(envFile, """
        STRING_VALUE=hello=world=test
        INT_VALUE=42
        """);

    EnvFileOptions options = OptionsFactory.create(EnvFileOptions.class,
        "--envFile=" + envFile.toString());

    assertThat(options.stringValue(), is("hello=world=test"));
  }

  @Test
  void missingFileIsHandledGracefully() {
    // Should not throw, just use defaults
    EnvFileOptionsWithDefaults options = OptionsFactory.create(EnvFileOptionsWithDefaults.class,
        "--envFile=/nonexistent/path/.env");

    assertThat(options.stringValue(), is("default_string"));
    assertThat(options.intValue(), is(0));
  }

  @Test
  void variableReferencesAreResolved() throws IOException {
    Path envFile = tempDir.resolve("refs.env");
    Files.writeString(envFile, """
        BASE_VALUE=hello
        STRING_VALUE=${BASE_VALUE}
        INT_VALUE=42
        """);

    EnvFileOptions options = OptionsFactory.create(EnvFileOptions.class,
        "--envFile=" + envFile);

    assertThat(options.stringValue(), is("hello"));
  }

  @OptionsExtensions(EnvironmentFile.class)
  public interface EnvFileOptions extends Options<EnvFileOptions> {

    @Option
    String stringValue();

    @Option
    int intValue();
  }

  @OptionsExtensions(EnvironmentFile.class)
  public interface EnvFileOptionsWithDefaults extends Options<EnvFileOptionsWithDefaults> {

    @Option
    default String stringValue() {
      return "default_string";
    }

    @Option
    int intValue();
  }
}