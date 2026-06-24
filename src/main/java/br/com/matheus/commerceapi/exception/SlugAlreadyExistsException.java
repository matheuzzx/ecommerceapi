package br.com.matheus.commerceapi.exception;

public class SlugAlreadyExistsException extends AlreadyExistsException {
    public SlugAlreadyExistsException(String slug) {
        super("Slug: " + slug + " already exists");
    }
}
