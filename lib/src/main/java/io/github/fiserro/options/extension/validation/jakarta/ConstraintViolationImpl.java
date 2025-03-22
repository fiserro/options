package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.OptionsBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConstraintViolationImpl implements ConstraintViolation<OptionsBuilder<?>> {

  private final String message;
  private final Object invalidValue;
  private final OptionDef optionDef;
  private final OptionsBuilder<?> options;

  @Override
  public String getMessageTemplate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public OptionsBuilder<?> getRootBean() {
    return options;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<OptionsBuilder<?>> getRootBeanClass() {
    return (Class<OptionsBuilder<?>>) options.getClass();
  }

  @Override
  public Object getLeafBean() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] getExecutableParameters() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getExecutableReturnValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path getPropertyPath() {
    return optionDef.path();
  }

  @Override
  public ConstraintDescriptor<?> getConstraintDescriptor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <U> U unwrap(Class<U> aClass) {
    throw new UnsupportedOperationException();
  }
}
