package com.koramarket.common.exceptions;

public class ValidationException extends BusinessException {
    public ValidationException() {
        super("Erreur de validation.");
    }
    public ValidationException(String message) {
        super(message);
    }
}
