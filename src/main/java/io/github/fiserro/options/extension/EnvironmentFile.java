package io.github.fiserro.options.extension;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Extension that loads option values from a .env file. The file format is one environment variable
 * per line in the form: {@code OPTION_NAME=optionValue}.
 * <p>
 * The default file name is {@code .env}, but can be overridden by:
 * <ul>
 *   <li>Program argument: {@code --envFile=<path>}</li>
 *   <li>Environment variable: {@code ENV_FILE}</li>
 * </ul>
 * <p>
 * The extension loads values into {@link Envio#setVar(String, String)} first, then uses
 * {@link EnvironmentVariableLoader} to set them on the options builder.
 */
@Slf4j
public class EnvironmentFile extends OptionsExtensionSettingValues {

  private static final String DEFAULT_ENV_FILE = ".env";
  private static final String ENV_FILE_ARG_PREFIX = "--envFile=";
  private static final String ENV_FILE_VAR = "ENV_FILE";

  public EnvironmentFile(Class<? extends Options<?>> declaringClass) {
    super("environment variable file", OptionExtensionType.LOAD_FROM_FILE, declaringClass);
  }

  @Override
  public void extend(OptionsBuilder<?, ?> options) {
    String envFilePath = resolveEnvFilePath(options.args());
    Path path = Path.of(envFilePath);

    if (!Files.exists(path)) {
      log.debug("Environment file '{}' not found, skipping", envFilePath);
      return;
    }

    loadEnvFile(path);

    EnvironmentVariableLoader.loadAndSetValues(options,
        (value, env, pathArr) -> setValueWithSource(options, value, env, envFilePath, pathArr));
  }

  private String resolveEnvFilePath(String[] args) {
    // First check program arguments
    for (String arg : args) {
      if (arg.startsWith(ENV_FILE_ARG_PREFIX)) {
        return arg.substring(ENV_FILE_ARG_PREFIX.length());
      }
    }

    // Then check environment variable
    String envFileFromEnv = Envio.getVar(ENV_FILE_VAR);
    if (envFileFromEnv != null && !envFileFromEnv.isEmpty()) {
      return envFileFromEnv;
    }

    // Default
    return DEFAULT_ENV_FILE;
  }

  private void loadEnvFile(Path path) {
    try (Stream<String> lines = Files.lines(path)) {
      lines
          .filter(line -> !line.isBlank())
          .filter(line -> !line.startsWith("#"))
          .forEach(this::parseAndSetEnvVar);
    } catch (IOException e) {
      log.warn("Failed to read environment file '{}': {}", path, e.getMessage());
    }
  }

  private void parseAndSetEnvVar(String line) {
    int equalsIndex = line.indexOf('=');
    if (equalsIndex <= 0) {
      log.warn("Invalid line in environment file, expected format 'NAME=value': {}", line);
      return;
    }

    String name = line.substring(0, equalsIndex).trim();
    String value = line.substring(equalsIndex + 1);

    // Only set if not already defined (env vars and args take precedence)
    if (Envio.getVar(name) == null) {
      log.debug("Setting environment variable '{}' from file", name);
      Envio.setVar(name, value);
    }
  }

  private void setValueWithSource(OptionsBuilder<?, ?> options, Object value, String envName,
      String envFilePath, String[] path) {
    if (value == null) {
      return;
    }
    // Include both the env var name and file path in the source name
    setValue(options, value, envName + " (file: " + envFilePath + ")", path);
  }
}