from dataclasses import dataclass, field
from enum import Enum
from pathlib import Path
from typing import List


class ProofResult(Enum):
    VERIFIED = 0
    FAILED = 1
    NONEXISTENT = 2
    PARTIAL = 3


@dataclass(frozen=True)
class VerificationResult:

    bbm: str
    proof_result: ProofResult
    proof_file: Path = None

    def is_verified(self) -> bool:
        return self.proof_result is ProofResult.VERIFIED

    def __str__(self) -> str:
        return " : ".join([self.bbm, str(self.proof_result)] + ([self.proof_file.name] if self.proof_file else []))


@dataclass(frozen=True)
class VerificationResults:

    results: List[VerificationResult] = field(default_factory=list)

    def append(self, res: VerificationResult):
        self.results.append(res)

    def __iter__(self):
        return iter(self.results)

    def __len__(self) -> int:
        return len(self.results)

    def __str__(self) -> str:
        return "\n".join(map(str, self.results))

    def for_hg(self, bbm: str) -> VerificationResult:
        return [r for r in self.results if r.bbm == bbm][0]
