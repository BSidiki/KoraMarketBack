-- table invoices
CREATE TABLE IF NOT EXISTS order_service.invoices (
  id uuid PRIMARY KEY,
  order_id uuid NOT NULL REFERENCES order_service.orders(id),
  invoice_number varchar(32) UNIQUE NOT NULL,
  url_pdf text,
  status varchar(32) NOT NULL,
  issued_at timestamp NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_invoices_order_id
  ON order_service.invoices(order_id);
