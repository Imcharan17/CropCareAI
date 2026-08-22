package com.cropportal.exception;

public class AiPredictionException extends RuntimeException {
    public AiPredictionException(String message) {
        super(message);
    }

    public AiPredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}
