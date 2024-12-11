package com.eastrobot.arch.llms.retry;

/**
 * <p></p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/8/15 18:27
 */
public class TransientAiException extends RuntimeException {
    public TransientAiException(String message) {
        super(message);
    }

    public TransientAiException(String message, Throwable cause) {
        super(message, cause);
    }
}
