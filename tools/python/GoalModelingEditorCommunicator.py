from pathlib import Path
from typing import List, Set

import TrackingReader
import json
from enum import Enum
from joana import JoanaToolResult
from dataclasses import dataclass

from proofs import VerificationResults, ProofResult, VerificationResult
from tracing import unrefine

from util import pprint

HARD_MECH_REL = 'Hard Mechanism Relationship'
STATUS = 'status'


@dataclass(frozen=True)
class SignatureIdentifying:
    componentId: str
    interfaceId: str
    signatureId: str

    def printIdentifying(self):
        print("ComponentId: %s -- InterfaceId: %s -- SignatureId: %s" % (self.componentId, self.interfaceId, self.signatureId))


class GoalModelingEditorCommunicator:

    def __init__(self, goalEditorModelPath: Path, trackingReader: TrackingReader):
        self.trackingReader = trackingReader
        self.goalEditorModelPath = goalEditorModelPath
        self.goalEditorModel: dict = json.load(goalEditorModelPath.open())
        self.hgStatus: dict = self.goalEditorModel[HARD_MECH_REL][STATUS]

    def annotate(self, analyzedFlowTag: str, flowResult: JoanaToolResult, proofs: VerificationResults):
        assert analyzedFlowTag == flowResult.flow, "Flow tags and results should be equal"

        flowTracking = self.trackingReader.getFlowTrackingElementById(flowResult.flow)

        sinks: Set[str] = {d["signatureId"] for d in flowTracking["sinks"]}

        hgsForFlow = self.trackingReader.getHGsOfJOANAFLowByTag(flowResult.flow)

        sourceSignatureId = flowResult.source

        sourceSignatureOriginId: str = unrefine(sourceSignatureId)
        #Not pretty robust because having several transformations are not possible due to direct mapping back to the tracking
        for originIdOfSignature in sourceSignatureOriginId:
            if not originIdOfSignature == flowTracking["source"]["signatureId"]:
                continue

            sourceIdentifying = SignatureIdentifying(flowTracking["source"]["componentId"], interfaceId = flowTracking["source"]["interfaceId"], signatureId= originIdOfSignature)

            service = self.trackingReader.getServiceById(sourceIdentifying.componentId)

            assert service is not None

            for reachedSink in flowResult.reached_sinks:
                self.annotateWithSink(flowTracking, hgsForFlow, sourceIdentifying, reachedSink, sinkHit=True, proofs=proofs)

            for unreachedSink in flowResult.unreached_sinks:
                self.annotateWithSink(flowTracking, hgsForFlow, sourceIdentifying, unreachedSink, sinkHit=False,
                                  proofs=proofs)

            for sink in (sinks - flowResult.possible_sinks):
                self.annotateWithSink(flowTracking, hgsForFlow, sourceIdentifying, sink, sinkHit=False, proofs=proofs)

    def annotateWithSink(self, flowTrackingElement: dict, hgsForFlow: List[dict],
                         sourceIdentifying: SignatureIdentifying,
                         sinkId: str, sinkHit: bool, proofs: VerificationResults):
        sinkOriginId: str = unrefine(sinkId)
        fittingSinkFound = False
        for sinkTrackingElement in flowTrackingElement["sinks"]:
            for originalSinkId in sinkOriginId:
                if sinkTrackingElement["signatureId"] == originalSinkId:
                    sinkSignatureIdentifying = SignatureIdentifying(sinkTrackingElement["componentId"],
                                                                sinkTrackingElement['interfaceId'],
                                                                originalSinkId)

                    for hg in hgsForFlow:
                        if self.hgFitsSource(hg, sourceIdentifying) and self.hgFitsSink(hg, sinkSignatureIdentifying):
                            self.modifyStatusByName(hg["name"], sinkHit,
                                                ProofResult.VERIFIED,
                                                proofs.for_hg(hg["bbm"]["name"]))
                            fittingSinkFound = True
                            break
                if (fittingSinkFound):
                    break

    def hgFitsSource(self, hg: dict, signatureIdentifying: SignatureIdentifying) -> bool:
        serviceName = self.trackingReader.getServiceById(signatureIdentifying.componentId).get('name');
        return hg.get('serviceName') == serviceName and hg.get('functionalRequirement').get(
            'id') == signatureIdentifying.interfaceId and next(iter(hg.get('functionalRequirement').get(
            'operationSignatures').keys())) == signatureIdentifying.signatureId

    def hgFitsSink(self, hg: dict, signatureIdentifying: SignatureIdentifying) -> bool:
        bbm = hg["bbm"]
        return bbm.get('id') == signatureIdentifying.componentId and bbm[
            "primaryInterfaceId"] == signatureIdentifying.interfaceId and next(iter(bbm["providedBBMInterfaces"][0].get(
            'operationSignatures').keys())) == signatureIdentifying.signatureId

    def modifyStatusByName(self, hgName: str, foundFlow: bool, verificationResult: ProofResult,
                           proof_result: VerificationResult):
        self.hgStatus[hgName] = self.calculateCommunicationCodeToValue(foundFlow, verificationResult, proof_result)

    def calculateCommunicationCodeToValue(self, foundFlow: bool, verificationResult: ProofResult,
                                          proof_result: VerificationResult) -> int:
        if (foundFlow is True and verificationResult.VERIFIED and proof_result.is_verified()):
            return CommunicationCodes.INTEGRATION_CORRECT_IMPLEMENTATION_CORRECT.value
        elif (foundFlow is False and verificationResult.VERIFIED and proof_result.is_verified()):
            return CommunicationCodes.INTEGRATION_INCORRECT_IMPLEMENTATION_CORRECT.value
        elif (foundFlow is True and (
                verificationResult.FAILED or verificationResult.PARTIAL or not proof_result.is_verified())):
            return CommunicationCodes.INTEGRATION_CORRECT_IMPLEMENTATION_INCORRECT.value
        elif (foundFlow is False and (
                verificationResult.FAILED or verificationResult.PARTIAL or not proof_result.is_verified())):
            return CommunicationCodes.INTEGRATION_INCORRECT_IMPLEMENTATION_INCORRECT.value
        else:
            return CommunicationCodes.NOT_ANALYZED.value

    def store(self):
        self.goalEditorModel[HARD_MECH_REL][STATUS] = self.hgStatus
        pprint(self.hgStatus)
        with self.goalEditorModelPath.open("w") as f:
            json.dump(self.goalEditorModel, f)


class CommunicationCodes(Enum):
    INTEGRATION_CORRECT_IMPLEMENTATION_CORRECT = 0
    INTEGRATION_INCORRECT_IMPLEMENTATION_CORRECT = 1
    INTEGRATION_CORRECT_IMPLEMENTATION_INCORRECT = 2
    INTEGRATION_INCORRECT_IMPLEMENTATION_INCORRECT = 3
    NOT_ANALYZED = 4
