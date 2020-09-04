import dataclasses
import functools
import json
import logging
import os
from dataclasses import dataclass
from pathlib import Path
from typing import Union, List

try:
    from prettyprinter import cpprint as pprint, prettyprinter

    prettyprinter.install_extras(
        include=[
            'dataclasses',
        ],
        warn_on_error=True
    )
except ImportError:
    from pprint import pprint


CONFIG_LOCATION = "config.json"


@dataclass(frozen=True)
class Config:

    @property
    def base_folder(self) -> Path:
        return Path(__file__).parent.parent.parent

    @functools.lru_cache()
    def abs_path(self, path: Union[str, Path]) -> Path:
        """ Relative to the base folder or not """
        while "{" in str(path):
            path = str(path).format(**self.to_dict())
        if not Path(path).is_absolute():
            return self.base_folder / path
        return Path(path)

    threads: int = None

    code_gen_folder: str = "CodeGenerations/RefinedPCMGen"

    src_gen_folder: str = "{code_gen_folder}/src-gen"

    """ 
    Has to contain sub folders with annotated java files, either relative to the demonstrator
    base folder or absolute 
    """
    pcc_gen_folder: str = "{src_gen_folder}/PCC"

    joana_build_folder: str = None
    joana_analyze_all: bool = False
    joana_reevaled_flows: List[str] = None
    joana_fully_uninitialized: bool = False

    tracking_file: str = "PCC-Models/PCMAndFlowModel-EditorModelGenerated/mod-gen/PCCTracking.json"
    tracing_file: str = "{code_gen_folder}/TracingLog.json"
    java_paths_log_file: str = "{code_gen_folder}/GeneratedPathsLogFile.txt"
    proof_files_folder: str = "tools/key/proofs"
    editor_model_path: str = "PCC-Models/EditorModel/PCC.json"
    pcm_refined_repository_path: str = "PCC-Models/PCM-Model-Complete/PCC.repository"
    pcm_refined_system_path: str = "PCC-Models/PCM-Model-Complete/TotalSystem.system"

    """
    Extension for also using the Access Analysis on the Palladio Model
    """

    haskalladio_tool_folder: str = "tools/haskalladio/XSB/"
    haskalladio_analysis_type: str = "queries-justify.result.json"


    @classmethod
    def load(cls, path: Path) -> 'Config':
        with path.open() as f:
            return cls.from_dict(json.load(f))

    @classmethod
    def from_dict(cls, d: dict) -> 'Config':
        return Config(**d)

    def store(self, path: Path):
        with path.open("w") as f:
            json.dump(self.to_dict(), f, indent=2, sort_keys=True)

    def to_dict(self) -> dict:
        return dataclasses.asdict(self)


@functools.lru_cache()
def get_config(path: Union[str, Path] = CONFIG_LOCATION) -> Config:
    """ Loads the config, if not present, create config and log as warning """
    p = Config().abs_path(path)
    if p.exists():
        return Config.load(p)
    conf = Config()
    conf.store(p)
    logging.warning("Created missing config file in {}".format(path))
    return conf


def abs_path(path: Union[str, Path]) -> Path:
    return get_config().abs_path(path)


def open_path(path: Union[str, Path], mode: str = "r") -> open:
    return open(str(abs_path(path)), mode)


def read_path(path: Union[str, Path]) -> str:
    with open_path(path) as f:
        return f.read()


def files(folder: Path) -> List[Path]:
    """ Returns a list of files in the passed folder and its sub folders"""
    return [(Path(root) / f)
            for root, directory, fs in os.walk(abs_path(folder))
            for f in fs
            if (Path(root) / f).is_file()]


if __name__ == '__main__':
    pprint(get_config().to_dict())
