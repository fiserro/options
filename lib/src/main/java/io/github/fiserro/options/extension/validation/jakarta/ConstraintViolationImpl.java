package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.Options;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConstraintViolationImpl<T> implements ConstraintViolation<T> {

  private final T rootBean;
  private final String message;
  private final Object invalidValue;
  private final OptionDef optionDef;
  private final Options options;

  @Override
  public String getMessageTemplate() {
    return "";
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<T> getRootBeanClass() {
    return (Class<T>) rootBean.getClass();
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
  public Path getPropertyPath() {
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
