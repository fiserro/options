package io.github.fiserro.options.test;

import io.github.fiserro.options.Option;

/**
 * Child interface that overrides parent's option with @Option annotation.
 * The override adds a different annotation and changes the default value.
 *
 * <p>This tests that:
 * <ul>
 *   <li>The child's annotation is visible via OptionDef.annotations()</li>
 *   <li>The parent's wither (withPower) is still accessible</li>
 *   <li>The child's default value is used</li>
 * </ul>
 */
public interface ChildWithOverride extends ParentModule<ChildWithOverride> {

    @Override
    @Option
    @ChildAnnotation("from-child")
    default int power() {
        return 75;  // Different default than parent
    }
}
