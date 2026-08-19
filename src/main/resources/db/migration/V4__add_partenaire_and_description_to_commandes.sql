ALTER TABLE commandes
    ADD COLUMN partenaire_id BIGINT NULL,
    ADD COLUMN description_article TEXT NULL;

ALTER TABLE commandes
    ADD CONSTRAINT fk_commandes_partenaire FOREIGN KEY (partenaire_id) REFERENCES partenaires (id);
