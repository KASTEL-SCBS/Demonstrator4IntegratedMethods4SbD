"""
Basic JOANA tool API using the scripts from ``tools/joana``
"""
import functools
import json
import logging
import multiprocessing
import os
import shutil
import subprocess
import sys
import tempfile
from enum import Enum
from pathlib import Path

import yaml
from typing import Dict, Any, List, NamedTuple, Optional, Set, Iterable, Tuple

from util import Config, pprint, get_config, abs_path


class JoanaFlow(NamedTuple):
    """ A single flow """
    source: str
    sink: str


class JoanaToolResult(NamedTuple):
    """ The results of the analysis """
    flow: str
    possible_sources: Set[str]
    possible_sinks: Set[str]
    flows: List[JoanaFlow]

    @classmethod
    def create(cls, flow: str, possible_sources: Iterable[str], possible_sinks: Iterable[str],
               flows: List[JoanaFlow]) -> 'JoanaToolResult':
        return JoanaToolResult(flow, set(possible_sources), set(possible_sinks),
                               flows)

    @property
    def reached_sinks(self) -> Set[str]:
        return {f.sink for f in self.flows}

    @property
    def unreached_sinks(self) -> Set[str]:
        return self.possible_sinks - self.reached_sinks

    def to_dict(self) -> dict:
        return {"flow": self.flow, "possible_sources": list(self.possible_sources), "possible_sinks": list(self.possible_sinks),
                "flows": [[f.source, f.sink] for f in self.flows]}

    @classmethod
    def from_dict(cls, d: dict) -> 'JoanaToolResult':
        return cls.create(d["flow"], d["possible_sources"], d["possible_sinks"], [JoanaFlow(*l) for l in d["flows"]])

    @property
    def source(self) -> str:
        assert len(self.possible_sources) == 1
        return next(iter(self.possible_sources))


class JoanaToolResults(NamedTuple):

    results: Dict[str, JoanaToolResult]

    @classmethod
    def from_dict(cls, d: Dict[str, dict]) -> 'JoanaToolResults':
        return JoanaToolResults({flow: JoanaToolResult.from_dict(sub) for flow, sub in d.items()})

    def to_dict(self) -> Dict[str, dict]:
        return {flow: result.to_dict() for flow, result in self.results.items()}

    def update(self, other: 'JoanaToolResults'):
        self.results.update(other.results)


class UninitializedMode(Enum):
    """
    Different modes for handling uninitialized fields, use ALL if possible (might use 90 GB and several hours per raw
    tag), use the other modes just for testing.
    """

    MINIMAL = ".*Ledu.*"
    BASIC = "({}|.*Ljava.*)".format(MINIMAL)
    ALL = ".*"


class JoanaTool:
    """
    Runs the Joana tool chain using the scripts from
    the ``tools/joana`` folder.
    """

    def __init__(self, svi_base_folder: Path, src_gen_folder: Path, build_folder: Path = None,
                 clean_build_folder: bool = None, log_commands: bool = False, flow_folder: Path = None,
                 use_old_flows: bool = False, unmode: Optional[UninitializedMode] = None):
        self.svi_base_folder = svi_base_folder
        self.build_folder = build_folder  # type: Optional[Path]
        self.src_gen_folder = src_gen_folder
        self.is_build_folder_tmp = False
        self.clean_build_folder = clean_build_folder
        self.log_commands = log_commands
        self.flow_folder = flow_folder
        self.use_old_flows = use_old_flows
        if not clean_build_folder:
            self.clean_build_folder = bool(build_folder)
        self.unmode = unmode

    def setup(self, src_gen_folder: Path):
        if not self.build_folder:
            self.build_folder = Path(tempfile.mkdtemp())
            self.is_build_folder_tmp = True
        elif self.clean_build_folder:
            self._remove_build_folder()
        if self.clean_build_folder or self.is_build_folder_tmp or not self.build_folder.exists():
            self._execute_tool("build_pcc", src_gen_folder, self.build_folder)
            self.is_build_folder_tmp = True
        if self.flow_folder:
            self.flow_folder.mkdir(exist_ok=True)

    @functools.lru_cache()
    def raw_tags(self) -> List[str]:
        """
        :return: all raw entry point tags
        """
        return [l.split(":")[0] for l in self._exec_joana("entry listAnnotated").split("\n")]

    def entry_per_tag(self, tag: str) -> Optional[str]:
        return ([l.split(":")[1] for l in self._exec_joana("entry listAnnotated").split("\n") if
                 l.split(":")[0].startswith(tag)] + [None])[0]

    @functools.lru_cache()
    def flows(self) -> Set[str]:
        """
        :return: flow tags
        """
        return {t.split("#")[0] for t in self.raw_tags()}

    def raw_tags_per_flow(self, flow: str) -> List[str]:
        return [t for t in self.raw_tags() if t.startswith(flow + "#")]

    @functools.lru_cache()
    def annotated_sources(self, flow: str) -> Set[str]:
        """ :return ids of the annotated sources """
        return {t.split("#")[1] for t in self.raw_tags_per_flow(flow)}

    @functools.lru_cache()
    def annotated_sinks(self, flow: str) -> Set[str]:
        """ :return ids of the annotated sinks """
        return {t.split("#")[2] for t in self.raw_tags_per_flow(flow)}

    def _exec_joana(self, cmd: str) -> str:
        _cmd = ["sh", str(self.svi_base_folder / "tools/joana/joana_pcc_raw"), str(self.build_folder), "-i"]
        if self.log_commands:
            print(" ".join("'{}'".format(c) for c in _cmd) + f" < '{cmd}\\n'")
        env = os.environ.copy()
        if self.unmode:
            env["JOANA_UNINITIALIZED"] = self.unmode.value
        return subprocess.check_output(_cmd, cwd=self.svi_base_folder, input=(cmd + "\n").encode(),
                                       stderr=subprocess.STDOUT,
                                       env=env).decode().replace("joana> ", "").strip()

    @functools.lru_cache(maxsize=None)
    def analyze_flows(self, flow_tags: Tuple[str] = None, threads: int = None) -> JoanaToolResults:
        flows = self._analyze_raw_flows(tuple([r for f in flow_tags for r in self.raw_tags_per_flow(f)]
                                        if flow_tags else self.raw_tags()), threads)
        grouped = {}  # type: Dict[str, List[JoanaFlow]]
        for raw_tag in self.raw_tags():
            flow, source, sink = raw_tag.split("#")
            if flow not in grouped:
                grouped[flow] = []
            if raw_tag in flows:
                grouped[flow].append(flows[raw_tag])
        return JoanaToolResults({flow: JoanaToolResult.create(flow, self.annotated_sources(flow), self.annotated_sinks(flow), jflows) for
                flow, jflows in grouped.items()})

    def partial_load_flows(self, reevaluated_flows: Iterable[str]) -> JoanaToolResults:
        """
        Load the tool results from src_gen_folder / flows.json. But reevalute the passed flows.
        """
        res = self.load_flows()
        res.update(self.analyze_flows(tuple(reevaluated_flows)))
        self._store_flow_results(res)
        return res

    def store_flows(self, filename: Optional[Path] = None, threads: int = None):
        """
        Compute the flows and store them (doesn't recompute the flows if they are already computed)

        :param filename: if None: store in src_gen_folder/flows.json
        """
        self._store_flow_results(self.analyze_flows(threads), filename)

    def _store_flow_results(self, res: JoanaToolResults, filename: Optional[Path] = None):
        with (filename or self.src_gen_folder / "flows.json").open("w") as f:
            json.dump(res.to_dict(), f, indent=2)

    def load_flows(self) -> JoanaToolResults:
        """
        Load the tool results from src_gen_folder / flows.json. Does not need to be called in a `with` block.
        """
        with (self.src_gen_folder / "flows.json").open() as f:
            return JoanaToolResults.from_dict(json.load(f))

    def is_flow_file_present(self) -> bool:
        print((self.src_gen_folder / "flows.json").absolute())
        return (self.src_gen_folder / "flows.json").exists()

    @functools.lru_cache(maxsize=None)
    def analyze_flow(self, flow: str, threads: int = None) -> JoanaToolResult:
        flows = self._analyze_raw_flows(tuple(self.raw_tags_per_flow(flow)), threads)
        return JoanaToolResult.create(flow, self.annotated_sources(flow), self.annotated_sinks(flow),
                                      list(flows.values()))

    @functools.lru_cache(maxsize=None)
    def _analyze_raw_flows(self, raw_tags: Tuple[str], threads: int = None) -> Dict[str, JoanaFlow]:
        res = []
        if not threads:
            threads = min(max(  # assumes that one computation requires around 90 GiB
                int(int(subprocess.check_output("free --mega |awk '/^Mem:/{print $7}'", shell=True)) / 1024 / 90), 1),
                    len(raw_tags))
        if threads == 1:
            res = [self._analyze_raw_flow(t) for t in raw_tags]
        else:
            with multiprocessing.Pool(threads) as p:
                res = p.map(self._analyze_raw_flow, raw_tags)
        return {raw_tag: res[i] for i, raw_tag in enumerate(raw_tags) if res[i]}

    def _analyze_raw_flow(self, raw_tag: str) -> Optional[JoanaFlow]:
        output = None
        filename = None
        if self.flow_folder:
            filename = self.flow_folder / raw_tag
        if self.use_old_flows and filename.exists():
            output = filename.read_bytes().decode()
        else:
            output = self._execute_joana_pcc(raw_tag)
            if self.flow_folder:
                filename.write_text(output)
        pprint(output)
        if len(output.strip()) == 0:
            return
        res = yaml.safe_load(output)
        if bool(res["found_flows"]):
            assert len(res["flow"]) == 1
            return JoanaFlow(*raw_tag.split("#")[1:])

    def _execute_joana_pcc(self, raw_tag: str) -> str:
        res = self._exec_joana(f"run {raw_tag}").split("done.\ndone.\n")
        if len(res) == 1:
            return ""
        return res[1]

    def _execute_tool(self, tool: str, *args: Any) -> str:
        cmd = ["sh", str(self.svi_base_folder / ("tools/joana/" + tool)), *map(str, [*args])]
        if self.log_commands:
            print(" ".join("'{}'".format(c) for c in cmd))
        return subprocess.check_output(cmd, cwd=str(self.svi_base_folder),
                                       stderr=None if self.log_commands else subprocess.DEVNULL,
                                       shell=True)

    def __enter__(self) -> 'JoanaTool':
        self.setup(self.src_gen_folder)
        return self

    def __exit__(self, *args):
        self._remove_build_folder()

    def _remove_build_folder(self):
        if self.clean_build_folder:
            shutil.rmtree(self.build_folder, ignore_errors=True)


def create_joana_tool(src_gen_folder: Path, **kwargs) -> JoanaTool:
    """

    :param src_gen_folder: has to contain sub folders with annotated java files, either relative to the demonstrator
    base folder or absolute
    :param kwargs: passed directly to the constructor of JoanaTool
    """
    parent = Path(__file__).parent.parent.parent
    if not src_gen_folder.is_absolute():
        src_gen_folder = parent / src_gen_folder
    return JoanaTool(parent, src_gen_folder, **kwargs)


def analyze_flows(src_gen_folder: Path, threads: int = None, **kwargs) -> JoanaToolResults:
    """
    Analyze all flows, return the results and store them in the appropriate flows.json for later use.

    Warning: Only do this on computers with enough RAM (> 50 GB) and with enough time on your hands

    :param src_gen_folder: see create_joana_tool
    :param threads: threads to use
    :param kwargs: passed directly to the constructor of JoanaTool
    """
    joana = create_joana_tool(src_gen_folder, **kwargs)
    if joana.is_flow_file_present():
        logging.warning("Overriding previous flows.json for {}".format(src_gen_folder))
    with joana:
        res = joana.analyze_flows(threads)
        joana.store_flows()
        return res


def load_flows(src_gen_folder: Path, reevaled_flows: Iterable[str] = None, analyze_if_needed: bool = True,
               threads: int = None, **kwargs) -> JoanaToolResults:
    """
    Load the flows from the src_gen_folder if they are present. Else output a warning and try to recompute it, needs
    at least 50GB of RAM and a few hours.

    :param src_gen_folder:
    :param reevaled_flows: flows to reevaluate (analyze_if_needed has to be true)
    :param analyze_if_needed: if false: just throw an exception if the flows.json file cannot be loaded
    :param kwargs: passed directly to the constructor of JoanaTool
    """
    joana = create_joana_tool(src_gen_folder, **kwargs)
    if reevaled_flows:
        if analyze_if_needed is False:
            raise AssertionError("Need to reevaluate but not allowed")
        if not joana.is_flow_file_present():
            return analyze_flows(src_gen_folder, threads=threads, **kwargs)
        with joana:
            return joana.partial_load_flows(reevaled_flows)
    if joana.is_flow_file_present():
        return joana.load_flows()
    elif analyze_if_needed:
        return analyze_flows(src_gen_folder, threads=threads, **kwargs)
    else:
        msg = "No flows.json found in {} consider running the analysis again or obtaining the file from " \
              "the computation on another computer".format(src_gen_folder)
        logging.error(msg)
        raise BaseException()


@functools.lru_cache()
def flows(config: Config = None) -> JoanaToolResults:
    """
    Fully configured via the config
    """
    config = config or get_config()
    kwargs = {"threads": config.threads,
              "build_folder": config.joana_build_folder,
              "unmode": UninitializedMode.ALL if config.joana_fully_uninitialized else UninitializedMode.BASIC}
    if config.joana_analyze_all:
        return analyze_flows(Path(config.pcc_gen_folder), **kwargs)
    return load_flows(abs_path(config.pcc_gen_folder), reevaled_flows=config.joana_reevaled_flows, **kwargs)


if __name__ == '__main__':
    src_gen_path = abs_path(get_config().pcc_gen_folder)
    if len(sys.argv) == 2:
        src_gen_path = Path(sys.argv[1])
    print(src_gen_path)
    pprint(analyze_flows(src_gen_path, log_commands=True))

    """
    parent = Path(__file__).parent.parent.parent

    pcc = Path("CodeGenerations/RefinedPCMGen/src-gen/PCC")

    with JoanaTool(parent, pcc, Path("/tmp/tmp07"), flow_folder=parent / pcc / "flows", log_commands=True) as j:
        print(j.flows())
        print(j.raw_tags())
        pprint(j.analyze_flows())
    """