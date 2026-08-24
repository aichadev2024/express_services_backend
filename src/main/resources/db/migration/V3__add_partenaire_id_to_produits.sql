DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'produits' AND column_name = 'partenaire_id') THEN
        ALTER TABLE produits ADD COLUMN partenaire_id BIGINT NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_produits_partenaire') THEN
        ALTER TABLE produits ADD CONSTRAINT fk_produits_partenaire FOREIGN KEY (partenaire_id) REFERENCES partenaires (id);
    END IF;
END $$;
