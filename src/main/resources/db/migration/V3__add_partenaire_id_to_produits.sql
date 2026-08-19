ALTER TABLE produits
    ADD COLUMN partenaire_id BIGINT NULL;

ALTER TABLE produits
    ADD CONSTRAINT fk_produits_partenaire FOREIGN KEY (partenaire_id) REFERENCES partenaires (id);
