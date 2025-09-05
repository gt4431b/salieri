
SELECT extname, extversion, extnamespace::regnamespace
FROM pg_extension
WHERE extname = 'vector';

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS rag_chunks (
  id UUID PRIMARY KEY,
  content  TEXT NOT NULL,
  metadata JSONB,
  embedding public.vector(1536)  -- <-- note "public."
);

ALTER TABLE rag_chunks
  ADD COLUMN fts tsvector GENERATED ALWAYS AS (to_tsvector('english', content)) STORED;
CREATE INDEX IF NOT EXISTS rag_chunks_fts_idx ON rag_chunks USING gin (fts);

-- Option B: make sure public is on the search_path (session-level)
SET search_path = public, pg_catalog;

CREATE INDEX IF NOT EXISTS rag_chunks_metadata_gin
  ON rag_chunks USING gin ((metadata));

CREATE INDEX IF NOT EXISTS rag_chunks_embedding_hnsw
  ON rag_chunks USING hnsw (embedding vector_l2_ops);

ALTER TABLE rag_chunks
  ADD COLUMN IF NOT EXISTS fts tsvector
  GENERATED ALWAYS AS (to_tsvector('english', content)) STORED;

CREATE INDEX IF NOT EXISTS rag_chunks_fts_idx
  ON rag_chunks USING gin (fts);

  