package io.github.fiserro.options.extension.validation.jakarta;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConstraintViolationImpl<T> implements ConstraintViolation<T> {

  private final String message;
  private final String propertyPath;
  private final Object invalidValue;

  @Override
  public Path getPropertyPath() {
    return null; // TODO
  }


  @Override
  public String getMessageTemplate() {
    return "";
  }

  @Override
  public T getRootBean() {
    return null;
  }

  @Override
  public Class<T> getRootBeanClass() {
    return null;
  }

  @Override
  public Object getLeafBean() {
    return null;
  }

  @Override
  public Object[] getExecutableParameters() {
    return new Object[0];
  }

  @Override
  public Object getExecutableReturnValue() {
    return null;
  }

  @Override
  public ConstraintDescriptor<?> getConstraintDescriptor() {
    return null;
  }

  @Override
  public <U> U unwrap(Class<U> aClass) {
    return null;
  }
}
