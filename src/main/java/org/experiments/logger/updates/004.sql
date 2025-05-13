create table tbl_setting
(
    id          integer primary key,
    model_name  text not null,
    system_text text not null
);

insert into tbl_setting (model_name, system_text)
select tm.model_text, ts.system_text
from tbl_system ts
         inner join tbl_cache tc on ts.rowid = tc.system_id
         inner join tbl_model tm on tm.rowid = tc.model_id
group by ts.rowid, tm.rowid;

alter table tbl_cache
    add column setting_id;

update tbl_cache
set setting_id = (select ts.id
                  from tbl_setting ts
                           inner join tbl_model tm on ts.model_name = tm.model_text
                           inner join tbl_setting tss on ts.system_text = tss.system_text
                  where tss.rowid = system_id
                    and tm.rowid = model_id);

drop index tbl_cache_index;

create index tbl_cache_index ON tbl_cache(setting_id,"query");

alter table tbl_cache drop column system_id;

alter table tbl_cache drop column model_id;

drop table tbl_model;

drop table tbl_system;
