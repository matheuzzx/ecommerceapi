package br.com.matheus.commerceapi.exception;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super("User Not Found");
    }
}
