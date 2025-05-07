CREATE TABLE IF NOT EXISTS tbl_model (model_text TEXT NOT NULL UNIQUE);

CREATE TABLE IF NOT EXISTS tbl_ontology (ontology_text TEXT NOT NULL UNIQUE);

CREATE TABLE IF NOT EXISTS tbl_task (task_text TEXT NOT NULL UNIQUE);

CREATE TABLE IF NOT EXISTS tbl_system (system_text TEXT NOT NULL UNIQUE);

CREATE TABLE IF NOT EXISTS tbl_cache (
    model_id INTEGER NOT NULL REFERENCES tbl_model(ROWID),
    ontology_id INTEGER NOT NULL REFERENCES tbl_ontology(ROWID),
    task_id INTEGER NOT NULL REFERENCES tbl_task(ROWID),
    system_id INTEGER NOT NULL REFERENCES tbl_system(ROWID),
    query TEXT NOT NULL,
    result TEXT NOT NULL,
    bool_result BOOLEAN
);

CREATE INDEX IF NOT EXISTS tbl_cache_index ON tbl_cache(model_id,ontology_id,task_id,system_id,query);
