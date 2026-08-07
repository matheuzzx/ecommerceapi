package br.com.matheus.commerceapi.exception;

public class InvalidArgumentException extends BusinessException {
    public InvalidArgumentException(String message) {
        super(message);
    }
}
