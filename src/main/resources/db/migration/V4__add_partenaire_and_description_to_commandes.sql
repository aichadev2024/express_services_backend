DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'commandes' AND column_name = 'partenaire_id') THEN
        ALTER TABLE commandes ADD COLUMN partenaire_id BIGINT NULL;
    END IF;

    IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'commandes' AND column_name = 'description_article') THEN
        ALTER TABLE commandes ADD COLUMN description_article TEXT NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_commandes_partenaire') THEN
        ALTER TABLE commandes ADD CONSTRAINT fk_commandes_partenaire FOREIGN KEY (partenaire_id) REFERENCES partenaires (id);
    END IF;
END $$;
