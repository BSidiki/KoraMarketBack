CREATE TABLE IF NOT EXISTS order_service.payments (
  id uuid PRIMARY KEY,
  order_id uuid NOT NULL REFERENCES order_service.orders(id),
  provider varchar(64) NOT NULL,
  status varchar(32) NOT NULL,
  amount_authorized bigint NOT NULL,
  amount_captured bigint NOT NULL,
  currency char(3) NOT NULL,
  external_transaction_id varchar(128),
  idempotency_key varchar(80),
  failure_reason text,
  created_at timestamp NOT NULL,
  authorized_at timestamp,
  captured_at timestamp
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_idempotency_key
  ON order_service.payments (idempotency_key)
  WHERE idempotency_key IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_payments_ext_tx
  ON order_service.payments (external_transaction_id)
  WHERE external_transaction_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_payments_order_id
  ON order_service.payments (order_id);
