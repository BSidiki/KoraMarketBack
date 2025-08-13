ALTER TABLE order_service.orders ADD COLUMN idempotency_key varchar(80);
CREATE UNIQUE INDEX IF NOT EXISTS uk_orders_idem
  ON order_service.orders(idempotency_key)
  WHERE idempotency_key IS NOT NULL;
