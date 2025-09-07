GRANT USAGE ON SCHEMA public TO app_user;
-- optional if your app needs to CREATE tables in public:
GRANT CREATE ON SCHEMA public TO app_user;

-- make sure app sessions see public on the search_path:
ALTER DATABASE TARGET_DB SET search_path = public, pg_catalog;
-- or per-role:
ALTER ROLE app_user SET search_path = public, pg_catalog;

CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA app;

ALTER DATABASE TARGET_DB SET search_path = app, public, pg_catalog;
