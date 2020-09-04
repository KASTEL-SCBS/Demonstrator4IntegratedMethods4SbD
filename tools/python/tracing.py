import functools
import json
from pathlib import Path
from typing import List, Set

from util import get_config, abs_path


class Tracing:
    """ Refine and unrefine ids, names and signatures (name#method_name) """

    def __init__(self, trans: dict):
        self._trans: List[dict] = trans["RequiredTracings"]

    @classmethod
    @functools.lru_cache()
    def load(cls, path: Path = None) -> 'Tracing':
        path = path or abs_path(get_config().tracing_file)
        return Tracing(json.loads(path.read_text()))

    def refine(self, item: str) -> Set[str]:
        """ from → to """
        return self._resolve(item, "from", "to")

    def unrefine(self, item: str) -> Set[str]:
        """ to → from """
        return self._resolve(item, "to", "from")

    def _resolve(self, item: str, f: str = "from", t: str = "to") -> Set[str]:
        if "#" in item:
            return self._resolve_signature(item, f, t)
        res_id = self._resolve_id(item, f, t)
        if res_id == item:
            return self._resolve_name(item, f, t)
        return res_id

    def _resolve_name(self, name: str, f: str, t: str) -> Set[str]:
        return {d[t]["name"] for d in self._trans if f in d and d[f]["name"] == name} or {name}

    def _resolve_signature(self, signature: str, f: str, t: str) -> Set[str]:
        name, sig = signature.split("#")
        res_name = self._resolve_name(name, f, t)
        bel_tracingss = []
        for d in self._trans:
            if f in d and d[f]["name"] == name and "BelongingTracings" in d:
                bel_tracingss.append(d["BelongingTracings"])
            if d["ElementType"] == "Signature":
                bel_tracingss.append(d)
        return {"{}#{}".format(res_name, d[t]["name"]) for bel_tracings in bel_tracingss for d in bel_tracings if f in d and d[f]["name"] == sig} or {signature}

    def _gen_id_dicts(self, trans: List[dict] = None) -> List[dict]:
        trans = trans or self._trans
        return [s for d in trans
                for s in ([d] if "to" in d else [] and "id" in d["to"]) +
                (self._gen_id_dicts(d["BelongingTracings"]) if "BelongingTracings" in d else [])]

    @property
    def _id_ds(self) -> List[dict]:
        return self._gen_id_dicts()

    def _resolve_id(self, id: str, f: str, t: str) -> Set[str]:
        return {d[t]["id"] for d in self._id_ds if d[f]["id"] == id} or {id}


def refine(item: str) -> str:
    return Tracing.load().refine(item)


def unrefine(item: str) -> str:
    return Tracing.load().unrefine(item)
