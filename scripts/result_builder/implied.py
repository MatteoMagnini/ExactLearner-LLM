

from typing import Any, Dict, List
from collections import defaultdict
import pandas as pd

from oracle import Oracle


class Implied:
    def __init__(self, oracle: Oracle):
        self.oracle = oracle

    def build_result(self) -> pd.DataFrame:
        classes = self.oracle.get_classes()

        res_dict: Dict[str, List[Any]] = defaultdict(list)

        for a in classes:
            res_dict["_identifier_"].append(a)
            for b in classes:
                res_dict[b].append(self.oracle.is_sub_type(a, b))

        df = pd.DataFrame(
            res_dict
        )

        df = df.set_index("_identifier_")

        return TransitivityClosure().close(df)

class TransitivityClosure:
    def close(self, matrix: pd.DataFrame):
        df = matrix.copy()
        n = len(df)
        for k in range(n):
            for i in range(n):
                for j in range(n):
                    df.iloc[i,j] = (df.iloc[i,j] or (df.iloc[i,k] and df.iloc[k,j]))

        return df