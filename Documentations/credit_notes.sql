CREATE TABLE IF NOT EXISTS order_service.credit_notes (
  id uuid PRIMARY KEY,
  invoice_id uuid NOT NULL REFERENCES order_service.invoices(id),
  credit_number varchar(32) UNIQUE NOT NULL,
  amount bigint NOT NULL,
  currency char(3) NOT NULL,
  created_at timestamp NOT NULL
);
