package br.com.matheus.commerceapi.exception;

import br.com.matheus.commerceapi.domain.Email;

public class EmailAlreadyExistsException extends AlreadyExistsException{
    public EmailAlreadyExistsException(Email email) {
        super("email: " + email + " already exists");
    }
}
