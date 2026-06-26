package br.com.matheus.commerceapi.exception;

public class StoreAlreadyExists extends AlreadyExistsException {
    public StoreAlreadyExists() {
        super("Store already exists");
    }
}
