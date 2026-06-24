package br.com.matheus.commerceapi.exception;

public class EmailAlreadyExistsException extends AlreadyExistsException{
    public EmailAlreadyExistsException(String email) {
        super("email: " + email + " already exists");
    }
}
