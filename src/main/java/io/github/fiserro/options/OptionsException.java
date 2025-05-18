package io.github.fiserro.options;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class OptionsException extends RuntimeException {

    private final transient Options<?> options;

    public OptionsException(String message) {
        super(message);
        this.options = null;
    }

    public OptionsException(Options<?> options) {
        this.options = options;
    }

    public OptionsException(String message, Options<?> options) {
        super(message);
        this.options = options;
    }

    public OptionsException(String message, Throwable cause, Options<?> options) {
        super(message, cause);
        this.options = options;
    }

    public OptionsException(Throwable cause, Options<?> options) {
        super(cause);
        this.options = options;
    }
}
