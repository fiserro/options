package io.github.fiserro.options.extension.validation.jakarta;

import io.github.fiserro.options.OptionDef;
import io.github.fiserro.options.OptionsBuilder;
import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Payload;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ValidateUnwrappedValue;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;

@Getter
@RequiredArgsConstructor
public class ConstraintViolationImpl implements ConstraintViolation<OptionsBuilder<?>> {

  private final String message;
  private final Object invalidValue;
  private final OptionDef optionDef;
  private final OptionsBuilder<?> options;
  private final Annotation annotation;

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
    return new ConstraintDescriptor() {

      @Override
      public Annotation getAnnotation() {
        return annotation;
      }

      @Override
      public String getMessageTemplate() {
        throw new UnsupportedOperationException();
      }

      @Override
      public Set<Class<?>> getGroups() {
        throw new UnsupportedOperationException();      }

      @Override
      public Set<Class<? extends Payload>> getPayload() {
        throw new UnsupportedOperationException();      }

      @Override
      public ConstraintTarget getValidationAppliesTo() {
        throw new UnsupportedOperationException();      }

      @Override
      public List<Class<? extends ConstraintValidator>> getConstraintValidatorClasses() {
        throw new UnsupportedOperationException();      }

      @Override
      public Map<String, Object> getAttributes() {
        throw new UnsupportedOperationException();      }

      @Override
      public Set<ConstraintDescriptor<?>> getComposingConstraints() {
        throw new UnsupportedOperationException();      }

      @Override
      public boolean isReportAsSingleViolation() {
        throw new UnsupportedOperationException();      }

      @Override
      public ValidateUnwrappedValue getValueUnwrapping() {
        throw new UnsupportedOperationException();      }

      @Override
      public Object unwrap(Class type) {
        throw new UnsupportedOperationException();      }
    };
  }

  @Override
  public <U> U unwrap(Class<U> aClass) {
    throw new UnsupportedOperationException();
  }
}
