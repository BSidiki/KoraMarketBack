-- facture créée & url_pdf non nulle
SELECT id, order_id, invoice_number, url_pdf, status, issued_at
FROM order_service.invoices
WHERE order_id = '01acb149-26e7-466d-8065-86e5239033d8';

-- outbox (si activée)
SELECT topic, status, payload
FROM order_service.outbox_messages
ORDER BY created_at DESC
LIMIT 5;
