ALTER TABLE product_service.produits ADD COLUMN vendeur_id uuid;
CREATE INDEX IF NOT EXISTS idx_produits_vendeur_id ON product_service.produits(vendeur_id);

ALTER TABLE product_service.produits ADD COLUMN sku varchar(64);
CREATE UNIQUE INDEX IF NOT EXISTS uk_produits_sku ON product_service.produits(sku);

