ALTER TABLE payments
    DROP CONSTRAINT IF EXISTS payments_status_check;

ALTER TABLE payments
    ADD CONSTRAINT payments_status_check
        CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'CANCELED', 'REFUNDED'));

ALTER TABLE payments
    ADD COLUMN refunded_at timestamp;
