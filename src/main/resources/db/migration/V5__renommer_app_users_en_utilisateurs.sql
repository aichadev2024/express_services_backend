-- Le nom de la contrainte unique sur username a ete auto-genere par Hibernate
-- (ddl-auto: update, avant l'introduction de Flyway) et n'est pas garanti ;
-- on ne la renomme pas, seul le renommage de la table/colonnes compte fonctionnellement.
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'app_users') THEN
        ALTER TABLE app_users RENAME TO utilisateurs;
    END IF;
    IF EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'utilisateurs' AND column_name = 'username') THEN
        ALTER TABLE utilisateurs RENAME COLUMN username TO nom_utilisateur;
    END IF;
    IF EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'utilisateurs' AND column_name = 'password') THEN
        ALTER TABLE utilisateurs RENAME COLUMN password TO mot_de_passe;
    END IF;
END $$;
