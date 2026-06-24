package br.com.matheus.commerceapi.exception;

public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super("Invalid Credentials");
    }
}
