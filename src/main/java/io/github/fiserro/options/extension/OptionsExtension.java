package io.github.fiserro.options.extension;

import io.github.fiserro.options.Options;
import io.github.fiserro.options.OptionsBuilder;

/**
 * Interface for options extensions. You can extend the Options instance with your own extensions to
 * load values from different sources or to validate the values.<p>
 * <p>
 * Exclusive extensions cannot be combined with others of the same type. It means that if you use
 * two exclusive extensions of the same type, on the class that implements Options, it will throw an
 * exception during {@link io.github.fiserro.options.OptionsFactory#create(Class, String...)}.<p> If
 * you use a different extension of the same type on child interface, the parent one will be
 * replaced with the child one.<p>
 *
 * @see OptionsExtensions
 */
public interface OptionsExtension {

  /**
   * Applies extension to the Options instance.
   *
   * @param options the Options instance to extend
   */
  void extend(OptionsBuilder<? extends Options<?>, ?> options);

  /**
   * Returns the type of the extension. Some extensions are exclusive and cannot be combined with
   * others.
   *
   * @return the type of the extension
   */
  OptionExtensionType type();

  /**
   * Returns the class that declares the extension.
   *
   * @return the class that declares the extension
   */
  Class<? extends Options<?>> declaringClass();
}
