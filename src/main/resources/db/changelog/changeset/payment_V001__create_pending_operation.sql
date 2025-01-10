CREATE TABLE pending_operation
(
    id                     UUID PRIMARY KEY,
    source_account_id      UUID         NOT NULL,
    target_account_id      UUID         NOT NULL,
    idempotency_key        VARCHAR(255) NOT NULL UNIQUE,
    amount                 BIGINT       NOT NULL,
    currency               VARCHAR(3)   NOT NULL,
    status                 VARCHAR(20)  NOT NULL,
    category               VARCHAR(50)  NOT NULL,
    account_balance_status VARCHAR(20)  NOT NULL,
    clear_scheduled_at     TIMESTAMPTZ  NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT current_timestamp,
    updated_at             TIMESTAMPTZ           DEFAULT current_timestamp
);
