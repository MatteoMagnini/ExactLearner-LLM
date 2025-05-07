create view view_bool_result as
SELECT
    r.setting_id AS setting_id,
    r.query_id as query_id,
    (LOWER(r."result") LIKE '%true%')  AS result_t,
    (LOWER(r."result") LIKE '%false%')  AS result_f,
    (LOWER(a."result") LIKE '%true%')  AS actual
FROM
    tbl_result r
        inner
            JOIN
    tbl_result a
    ON
        r.query_id = a.query_id
        inner join tbl_query tq on a.query_id = tq.rowid
WHERE
    r.setting_id != a.setting_id and a.setting_id = (select rowid from tbl_setting where answer = true);

create index index_tbl_result_setting on tbl_result (setting_id);

create index index_tbl_result_query on tbl_result (query_id);

create index index_tbl_query_test on tbl_query (test_id);

create index index_tbl_query_ontology on tbl_query (ontology);
