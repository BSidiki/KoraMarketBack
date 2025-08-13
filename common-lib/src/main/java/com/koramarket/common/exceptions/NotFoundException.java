package com.koramarket.common.exceptions;

public class NotFoundException extends BusinessException {
    public NotFoundException() {
        super("Ressource non trouvée.");
    }
    public NotFoundException(String message) {
        super(message);
    }
}
