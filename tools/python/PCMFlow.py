

from pcm_xml_parser import PCMResourceIdentifications
from pcm_xml_parser import PCM_XMLParser
from pcm_xml_parser import PCMResourceTypes
from pcm_xml_parser import ConnectorType
from pcm_xml_parser import DelegationConnector
from pcm_xml_parser import ReqProvRole
from pcm_xml_parser import OperationSignature
from pcm_xml_parser import SEFFActionType
from pcm_xml_parser import SEFFExternalCallAction
from pcm_xml_parser import AssemblyConnector
from pcm_xml_parser import SEFF
import TrackingReader
from typing import List
from joana import flows
import json

from util import get_config, open_path, abs_path, files, pprint

class PCMFlowAnalyzer:
    pcmXMLParser: PCM_XMLParser
    trackingReader: TrackingReader

    def __init__(self, pcmXMLParser:PCM_XMLParser, trackingReader: TrackingReader):
        self.pcmXMLParser = pcmXMLParser
        self.trackingReader = trackingReader


    def isSinkInFlow(self, sourceComponentId: str, sourceInterfaceId: str,sourceOperationId:str, sinkOperationId: str) -> bool:

        result:bool = False

        entryOperationPoint:dict = self.searchEntryOperationAndInterface(sourceInterfaceId=sourceInterfaceId, sourceOperationId=sourceOperationId)
        providedDelegationConnector: DelegationConnector = self.searchProvidedDelegationConnector(sourceComponentId=sourceComponentId, providedInterfaceRole=entryOperationPoint)
        targetSEFF: SEFF = self.searchSEFFFromDelegationConnector(delegationConnector=providedDelegationConnector, providedInterfaceRole=entryOperationPoint)


        providedOperationIdentifyingForSink:dict = self.findProvidedOperationIdentifyingForOperationId(sinkOperationId)
        if providedOperationIdentifyingForSink is None:
            return result

        for step in targetSEFF.steps:
            if step.actionType == SEFFActionType.EXTERNAL_CALL:
                tmp:SEFFExternalCallAction = step
                assemblyConnector: AssemblyConnector = self.findAssemblyConnectorForRequiredRole(tmp.calledRole)
                flowsForSource:List[dict] = self.findFlowForProvidedInterfaceOfAssemblyConnector(connector=assemblyConnector, targetOperation=tmp.calledService)

                if flowsForSource is not None:
                    targetSinkOperationIdentifying:OperationSignature = providedOperationIdentifyingForSink["OperationSignature"]

                    if targetSinkOperationIdentifying is not None:
                        filteredFlows: List[dict] = self.filterFlowsForSinkOperationId(joanaFlows=flowsForSource, targetOperation=targetSinkOperationIdentifying)
                        joanaFlows = flows()

                        for trackingFlow in filteredFlows:
                            flowForTag = joanaFlows.results[trackingFlow["joanaFlowId"]]
                            for joanaFlow in flowForTag.flows:
                                if joanaFlow.sink == sinkOperationId:
                                    result = True
                                    return result

        return result






    def searchEntryOperationAndInterface(self, sourceInterfaceId:str, sourceOperationId: str) -> dict:
        for system in self.pcmXMLParser.systems:
            for providedRole in system.providedRoles:
                for operation in providedRole.targetInterface.operationSignatures:
                    if providedRole.targetInterface.id == sourceInterfaceId and operation.id == sourceOperationId:
                        resultDict: dict = {
                            "OperationSignature" : operation,
                            "ProvidedRole": providedRole
                        }
                        return resultDict


    def searchProvidedDelegationConnector(self, sourceComponentId:str ,providedInterfaceRole:dict) -> DelegationConnector:
        for system in self.pcmXMLParser.systems:
            for connector in system.connectors:
                if connector.type == ConnectorType.PROVIDED_DELEGATION:
                    tmp: DelegationConnector = connector
                    if tmp.assemblyContext.encapsulatedComponent.id == sourceComponentId:
                        provRoleId = providedInterfaceRole["ProvidedRole"].id
                        if tmp.outerRole.id == provRoleId:
                            return tmp


    def searchSEFFFromDelegationConnector(self, delegationConnector:DelegationConnector, providedInterfaceRole:dict) -> SEFF:
        for seff in delegationConnector.assemblyContext.encapsulatedComponent.seffs:
            signature: OperationSignature = providedInterfaceRole["OperationSignature"]
            if seff.targetSignature.id == signature.id:
                return seff

    def findAssemblyConnectorForRequiredRole(self, role:ReqProvRole) -> AssemblyConnector:
        for system in self.pcmXMLParser.systems:
            for connector in system.connectors:
                if connector.type == ConnectorType.ASSEMBLY:
                    tmp: AssemblyConnector = connector
                    if tmp.requiredRole.id == role.id:
                        return tmp

    def findFlowForProvidedInterfaceOfAssemblyConnector(self, connector:AssemblyConnector, targetOperation:OperationSignature) -> List[dict]:
        joanaFlows = self.trackingReader.getJoanaFlowsBySourceIdentifying(componentId=connector.providingAssemblyContext.encapsulatedComponent.id, interfaceId=connector.providedRole.targetInterface.id, operationId=targetOperation.id)
        return joanaFlows

    def filterFlowsForSinkOperationId(self, joanaFlows: List[dict], targetOperation: OperationSignature):
        filteredFlows: List[dict] = []

        for flow in joanaFlows:
            for sink in flow["sinks"]:
                if sink["signatureId"] == targetOperation.id:
                    filteredFlows.append(flow)

        return filteredFlows

    def findProvidedOperationIdentifyingForOperationId(self, operationId:str) -> dict:
        for repository in self.pcmXMLParser.repositories:
            for component in repository.components:
                for providedRole in component.providedInterfaceRoles:
                    for operationSignature in providedRole.targetInterface.operationSignatures:
                        if operationSignature.id == operationId:
                            identifying: dict = {
                                "Component" : component,
                                "Interface" : providedRole.targetInterface,
                                "OperationSignature" : operationSignature
                             }

                            return identifying


if __name__ == '__main__':

    pccRepositoryResource: PCMResourceIdentifications = PCMResourceIdentifications(fileName="PCC.repository", path="PCC-Models/PCM-Model-Complete/PCC.repository", pcmResourceType=PCMResourceTypes.REPOSITORY)
    pccSystemResource: PCMResourceIdentifications = PCMResourceIdentifications(fileName="TotalSystem.system", path="PCC-Models/PCM-Model-Complete/TotalSystem.system", pcmResourceType=PCMResourceTypes.SYSTEM)

    resources = [pccRepositoryResource, pccSystemResource]

    parser = PCM_XMLParser()
    parser.parse(resources)
    trackingReader: TrackingReader = TrackingReader.TrackingReader(json.load(open_path(get_config().tracking_file)),
                                                            abs_path(get_config().src_gen_folder))
    flowAnalysis: PCMFlowAnalyzer = PCMFlowAnalyzer(pcmXMLParser=parser, trackingReader=trackingReader)
    flowAnalysis.isSinkInFlow(sourceComponentId="_Xc7BYMpWEemVkJoNuJTOzQ", sourceInterfaceId="_CsAIIG9SEeqa34g-QYI2IQ", sourceOperationId="_CsAIIW9SEeqa34g-QYI2IQ", sinkOperationId="_CsFAp29SEeqa34g-QYI2IQ")