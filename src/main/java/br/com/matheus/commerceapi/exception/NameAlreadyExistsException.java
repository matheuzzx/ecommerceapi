package br.com.matheus.commerceapi.exception;

public class NameAlreadyExistsException extends AlreadyExistsException {
    public NameAlreadyExistsException(String name) {
        super("Name already exists: " + name);
    }
}
