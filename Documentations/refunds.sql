-- VXX__refunds.sql
CREATE TABLE IF NOT EXISTS order_service.refunds (
  id uuid PRIMARY KEY,
  payment_id uuid NOT NULL REFERENCES order_service.payments(id) ON DELETE CASCADE,
  order_id uuid NOT NULL REFERENCES order_service.orders(id) ON DELETE CASCADE,
  amount bigint NOT NULL,
  currency char(3) NOT NULL,
  status varchar(32) NOT NULL,
  external_refund_id text UNIQUE,
  reason text,
  created_at timestamp NOT NULL,
  completed_at timestamp NULL
);

CREATE INDEX IF NOT EXISTS idx_refunds_order ON order_service.refunds(order_id);
CREATE INDEX IF NOT EXISTS idx_refunds_payment ON order_service.refunds(payment_id);
