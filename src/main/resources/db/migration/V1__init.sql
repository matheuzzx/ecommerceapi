-- Initial schema generated from the JPA entity model.
-- Baseline version for existing databases created with ddl-auto=update.

CREATE TABLE users (
    id integer NOT NULL,
    created_at timestamp,
    email varchar(255) NOT NULL,
    name varchar(50) NOT NULL,
    password_hash varchar(255),
    updated_at timestamp,
    user_role varchar(255) NOT NULL check (user_role in ('CUSTOMER','ADMIN','STOREOWNER')),
    primary key (id),
    unique (email)
);

CREATE TABLE addresses (
    id integer NOT NULL,
    city varchar(100) NOT NULL,
    number varchar(255),
    state varchar(2) NOT NULL,
    street varchar(255) NOT NULL,
    zip_code varchar(10) NOT NULL,
    user_id bigint NOT NULL,
    primary key (id)
);

CREATE TABLE categories (
    id integer NOT NULL,
    active boolean NOT NULL,
    created_at timestamp,
    description varchar(500) NOT NULL,
    display_name varchar(100) NOT NULL,
    name varchar(50) NOT NULL,
    updated_at timestamp,
    primary key (id),
    unique (name)
);

CREATE TABLE stores (
    id integer NOT NULL,
    active boolean NOT NULL,
    created_at timestamp,
    email varchar(100) NOT NULL,
    name varchar(100) NOT NULL,
    slug varchar(100) NOT NULL,
    updated_at timestamp,
    store_owner_id bigint UNIQUE,
    primary key (id),
    unique (email),
    unique (slug)
);

CREATE TABLE products (
    id integer NOT NULL,
    active boolean NOT NULL,
    created_at timestamp,
    description varchar(2000),
    name varchar(200) NOT NULL,
    price numeric(10,2) NOT NULL,
    updated_at timestamp,
    category_id bigint NOT NULL,
    store_id bigint NOT NULL,
    primary key (id)
);

CREATE TABLE stocks (
    id integer NOT NULL,
    created_at timestamp,
    last_updated timestamp,
    quantity integer NOT NULL,
    reserved integer NOT NULL,
    updated_at timestamp,
    product_id bigint NOT NULL,
    primary key (id),
    unique (product_id)
);

CREATE TABLE orders (
    id integer NOT NULL,
    created_at timestamp,
    date timestamp NOT NULL,
    city varchar(100) NOT NULL,
    number varchar(255),
    state varchar(2) NOT NULL,
    street varchar(255) NOT NULL,
    zip_code varchar(10) NOT NULL,
    status varchar(20) NOT NULL check (status in ('CREATED','PAID','SHIPPED','DELIVERED','CANCELED')),
    total numeric(10,2) NOT NULL,
    updated_at timestamp,
    customer_id bigint NOT NULL,
    store_id bigint NOT NULL,
    primary key (id)
);

CREATE TABLE order_items (
    id integer NOT NULL,
    created_at timestamp,
    quantity integer NOT NULL,
    unit_price numeric(10,2) NOT NULL,
    updated_at timestamp,
    order_id bigint NOT NULL,
    product_id bigint NOT NULL,
    primary key (id)
);

CREATE TABLE payments (
    id integer NOT NULL,
    amount numeric(10,2) NOT NULL,
    checkout_url varchar(255),
    created_at timestamp,
    method varchar(20) NOT NULL check (method in ('CREDIT_CARD','PIX','BOLETO')),
    paid_at timestamp,
    status varchar(20) NOT NULL check (status in ('PENDING','PAID','FAILED','CANCELED')),
    transaction_id varchar(64) NOT NULL,
    updated_at timestamp,
    order_id bigint NOT NULL,
    primary key (id),
    unique (transaction_id)
);
