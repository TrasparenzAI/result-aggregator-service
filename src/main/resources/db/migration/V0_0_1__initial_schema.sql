CREATE TABLE IF NOT EXISTS results_with_geo (
    id BIGSERIAL PRIMARY KEY,
    workflow_id TEXT NOT NULL,
    geo_json bytea,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    version INT DEFAULT 0);
