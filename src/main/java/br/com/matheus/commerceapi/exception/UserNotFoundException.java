package br.com.matheus.commerceapi.exception;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("User Not Found");
    }
}
