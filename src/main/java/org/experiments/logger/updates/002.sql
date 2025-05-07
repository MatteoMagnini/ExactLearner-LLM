CREATE TABLE IF NOT EXISTS tbl_new_cache (
    model_id INTEGER NOT NULL REFERENCES tbl_model(ROWID),
    system_id INTEGER NOT NULL REFERENCES tbl_system(ROWID),
    query TEXT NOT NULL,
    "result" TEXT NOT NULL,
    bool_result BOOLEAN
);

INSERT INTO tbl_new_cache (model_id, system_id, query, "result", bool_result)
    select model_id, system_id, query, "result", bool_result
    from tbl_cache
    group by model_id, system_id, query;

DROP INDEX tbl_cache_index;

DROP TABLE tbl_ontology;

DROP TABLE tbl_task;

DROP TABLE tbl_cache;

ALTER TABLE tbl_new_cache RENAME TO tbl_cache;

CREATE INDEX IF NOT EXISTS tbl_cache_index ON tbl_cache(model_id,system_id,query);
