import sqlite3
from typing import List, Protocol

import pandas as pd


class Oracle(Protocol):
    def is_sub_type(self, a: str, b: str) -> bool:
        ...

    def get_classes(self) -> List[str]:
        ... 

class ResultOracle:
    def __init__(self, test_id: int, setting_id: int, ontology: str, true_str: str = "true"):
        con = sqlite3.connect("./results-test.sqlite3")
        self.df = pd.read_sql(f"""select "result" as "response", tq.identifier as "query_ident" from tbl_result tr
                                inner join tbl_query tq on tr.query_id = tq.rowid
                                where test_id = {test_id} and ontology = '{ontology}' and setting_id = {setting_id}""", con)
        self.true_str = true_str
        self.query_count = 0
        con.close()
    
    def is_sub_type(self, a: str, b: str) -> bool:
        self.query_count += 1
        r = self.df[self.df["query_ident"] == f"{a} SubClassOf {b}"]
        r = r.iloc[0]
        return self.true_str in r["response"].lower()
    
    def get_classes(self) -> List[str]:
        df = self.df.copy()
        df["classes"] = df["query_ident"].str.split(" SubClassOf ").str[0]
        return df["classes"].unique().tolist()
