package br.com.matheus.commerceapi.exception;

public class StoreNotFoundException extends NotFoundException {
    public StoreNotFoundException() {
        super("Store not found");
    }
}
