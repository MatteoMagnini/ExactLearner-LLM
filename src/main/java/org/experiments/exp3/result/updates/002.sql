CREATE TABLE tbl_new_test (
    test_name TEXT NOT NULL,
    test_type TEXT NOT NULL
);

INSERT INTO tbl_new_test (test_name, test_type)
select test_name, test_type from tbl_test group by test_name, test_type;

CREATE TABLE tbl_setting (
    model TEXT,
    system_text TEXT,
    query_format TEXT,
    answer BOOLEAN NOT NULL
);

INSERT INTO tbl_setting (model, system_text, query_format, answer)
select model, system, query_format_name, FALSE from tbl_test group by model, system, query_format_name;

CREATE TABLE tbl_query (
    test_id INTEGER,
    identifier TEXT NOT NULL,
    ontology TEXT
);

INSERT INTO tbl_query (test_id, identifier, ontology)
select nt.ROWID, query_ident, ontology
from tbl_result r
    inner join tbl_test t on r.test_id = t.ROWID
    inner join tbl_new_test nt on nt.test_type = t.test_type and nt.test_name = t.test_name
group by query_ident, ontology;

CREATE TABLE tbl_new_result (
    query_id INTEGER NOT NULL,
    setting_id INTEGER NOT NULL,
    query_text TEXT,
    result TEXT NOT NULL
);

INSERT INTO tbl_new_result (query_id, setting_id, query_text, result)
select q.ROWID, s.ROWID, r.query_text, r.response
from tbl_result r
    inner join tbl_query q on q.identifier = r.query_ident
    inner join tbl_test t on t.ROWID = r.test_id
    inner join tbl_setting s
        on s.system_text = t.system and s.model = t.model and s.query_format = t.query_format_name
group by q.ROWID, s.ROWID, r.query_text, r.response;

drop table tbl_result;

drop table tbl_test;

alter table tbl_new_result rename to tbl_result;

alter table tbl_new_test rename to tbl_test;