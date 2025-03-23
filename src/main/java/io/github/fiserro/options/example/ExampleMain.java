package io.github.fiserro.options.example;

import io.github.fiserro.options.Option;
import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ExampleMain {

  public static void main(String[] args) {
    // Seznam, který chceme validovat
    List<Integer> numbers = List.of(1, 2, 3);

    // Vytvoření Validatoru
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    ExampleOptions options = OptionsFactory.create(ExampleOptions.class,
        Map.of("numbers", numbers, "number", 11));

    Set<ConstraintViolation<ExampleOptions>> violations = validator.validate(options);
    System.out.println(violations);
  }

  public interface ExampleOptions extends Options {
    @Size(min = 1, max = 3)
    @Option
    List<Integer> numbers();

    @Max(10)
    @Option
    int number();
  }
}
