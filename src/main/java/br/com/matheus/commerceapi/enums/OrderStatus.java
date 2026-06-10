package br.com.matheus.commerceapi.enums;

public enum OrderStatus {
    CREATED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELED;

    public OrderStatus next() {
        return switch (this) {
            case CREATED -> PAID;
            case PAID -> SHIPPED;
            case SHIPPED -> DELIVERED;
            case DELIVERED -> DELIVERED;
            case CANCELED -> CANCELED;
        };
    }
}
