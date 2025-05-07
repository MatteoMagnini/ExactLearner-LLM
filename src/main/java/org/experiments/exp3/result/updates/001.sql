CREATE TABLE tbl_test (
    model TEXT NOT NULL,
    system TEXT NOT NULL,
    ontology TEXT NOT NULL,
    query_format_name TEXT NOT NULL,
    test_type TEXT NOT NULL,
    test_name TEXT NOT NULL,
    test_nr INTEGER NOT NULL
);

CREATE TABLE tbl_result (
    test_id INTEGER NOT NULL REFERENCES tbl_test(ROWID),
    query_ident TEXT NOT NULL,
    query_text TEXT NOT NULL,
    response TEXT NOT NULL
);