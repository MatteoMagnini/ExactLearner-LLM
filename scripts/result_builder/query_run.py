
import sqlite3

import pandas as pd

from implied import Implied
from oracle import ResultOracle
from result_saver import ResultSaver

con = sqlite3.connect("./results.sqlite3")

test = 2

settings = pd.read_sql(f"""
                       select ROWID from tbl_setting ts where ts.rowid in
                        (select setting_id from tbl_result where query_id in (select rowid from tbl_query where test_id = {test}))""", con)["rowid"].tolist()

ontologies = pd.read_sql("select ontology from tbl_query tq where tq.test_id = 21 group by ontology ", con)["ontology"].tolist()

con.close()

saver = ResultSaver(test)
for o in ontologies:
    for s in settings:
        print(f"{o}-{s}")
        ro = ResultOracle(test, s, o)
        print("result")

        im = Implied(ro).build_result()
        print("fix")
        saver.insert_new_values(o, s, im)
        #im.to_csv(f"./notebooks/results/{s}-{o.split('/')[-1].split('.')[0]}.csv")
        print("fixed")
