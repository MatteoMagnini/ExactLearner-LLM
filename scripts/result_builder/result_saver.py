import sqlite3
import pandas as pd

class ResultSaver:

    def __init__(self, test_id: int):
        self.con = sqlite3.connect("./results.sqlite3")
        cursor = self.con.cursor()
        cursor.execute("""
                insert into tbl_test (test_name, test_type) values (?, ?)
            """, (f"{test_id}FullClassFix", "fixFullClass"))

        new_test_id = cursor.lastrowid
        queries = pd.read_sql(f"""
            select * from tbl_query where test_id = {test_id}
        """, self.con)

        queries["test_id"] = new_test_id

        q_rows = list(queries[["test_id", "identifier", "ontology"]].itertuples(index=False, name=None))

        cursor.executemany("""
                insert into tbl_query (test_id, identifier, ontology) values (?, ?, ?)
        """, q_rows)

        cursor.close()

        self.test_id = new_test_id

        self.con.commit()

    def insert_new_values(self, ontology: str, setting_id: int, df: pd.DataFrame):
        queries = pd.read_sql(f"""
            select rowid, identifier from tbl_query where test_id = {self.test_id} and ontology = '{ontology}'
        """, self.con)

        for (query_id, identifer) in queries.itertuples(index=False, name=None):
            l = identifer.split(" SubClassOf ")
            a = l[0]
            b = l[1]
            self.con.execute("""
                insert into tbl_result (query_id, setting_id, query_text, result) values (?, ?, ?, ?)
            """, (query_id, setting_id, "", str(df[b][a])))
        self.con.commit()
