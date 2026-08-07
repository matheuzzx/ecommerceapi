package br.com.matheus.commerceapi.exception;

public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(message);
    }
}
