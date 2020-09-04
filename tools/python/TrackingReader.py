import os
from pathlib import Path
from typing import List, Optional

from util import files


class TrackingReader:
    trackingInfo: dict
    javaElements: str

    def __init__(self, trackingInfo, pathToDirectory: Path):
        self.trackingInfo = trackingInfo
        self.javaElements = files(pathToDirectory)

    def getAllAppliedBBMs(self):
        resultBBM = []

        for service in self.trackingInfo['services']:
            for bbm in service.get('blackBoxMechanisms'):
                bbmName = bbm.get('name')
                if not bbmName in resultBBM:
                    resultBBM.append(bbmName)

        return resultBBM


    def getAllJOANAFlows(self) -> List[dict]:
        return [t for t in self.trackingInfo.get('extensionTrackingElements') if "joanaFlowId" in t]


    def getAllJoanaFlowTags(self):
        flowTags = []

        for flow in self.getAllJOANAFlows():
            flowTags.append(flow["joanaFlowId"])

        return flowTags

    def getBBMNamesForFlowByTag(self, tag):
        usedBBMNames = []

        for flow in self.getAllJOANAFlows():
            if flow["joanaFlowId"] == tag:
                for sink in flow["sinks"]:
                    usedBBMNames.append(self.findBBMNameById(sink["signatureId"]))
                return usedBBMNames


    def getHGs(self, componentId, functionalRequirementId):
        targetHGs = []
        for service in self.trackingInfo.get('services'):
            if service.get('id') == componentId:
                for hg in service.get('hardGoals'):
                    functionalRequirement = hg.get('functionalRequirement')
                    #softGoal = hg.get('SoftGoal')
                    #softGoalAsset = softGoal.get('asset')
                    if functionalRequirement.get('id') == functionalRequirementId:
                        targetHGs.append(hg)

        return targetHGs

    def getHGsOfJOANAFLowByTag(self, tag):

        flow = self.getJoanaFlowByTag(tag)
        componentId = flow["source"]["componentId"]
        functionalRequirementId = flow["source"]["interfaceId"]

        return self.getHGs(componentId, functionalRequirementId)

    def getServiceById(self, id: str) -> dict:
        for service in self.trackingInfo.get('services'):
            if service.get('id') == id:
                return service

    def getFlowTrackingElementById(self, id: str) -> dict:
        for element in self.trackingInfo.get("extensionTrackingElements"):
            if element.get('joanaFlowId') == id:
                return element

    def getJoanaFlowByTag(self, tag):
        for flow in self.getAllJOANAFlows():
            if flow['joanaFlowId'] == tag:
                return flow

    def findBBMNameById(self, id: str) -> str:
        return self.findBBMById(id)["name"]

    def findBBMById(self, id: str) -> Optional[dict]:
        for s in self.trackingInfo["services"]:
            for bbm in s["blackBoxMechanisms"]:
                for inter in bbm["providedBBMInterfaces"]:
                    if id in inter["operationSignatures"]:
                        return bbm

    def getPathsToBBMJavaFiles(self, bbmName):
        lines = map(str, self.javaElements)

        paths = ()

        bbmInterfacePath = ""
        bbmClassPath = ""

        for line in lines:

            tmp = bbmName.replace(" ", "")
            if tmp in line:
                if('interfaces' in line):
                    bbmInterfacePath = line
                if ('components' in line):
                    bbmClassPath = line
                if ((bbmInterfacePath != "")  & (bbmClassPath != "")):
                    paths = (bbmClassPath,bbmInterfacePath)
                    return paths

        return paths

    def getPathsToBBMJavaFilesBySearchingDirectory(self, bbmName, dirPath: Path):
        self.javaElements = files(dirPath)
        return self.getPathsToBBMJavaFiles(bbmName)

    def getJoanaFlowsBySourceIdentifying(self, componentId: str, interfaceId:str, operationId: str) -> dict:
        joanaFlows: List[dict] = self.getAllJOANAFlows()

        flows: List[dict] = []

        for flow in joanaFlows:
            if componentId == flow["source"]["componentId"] and interfaceId == flow["source"]["interfaceId"] and operationId == flow["source"]["signatureId"]:
                flows.append(flow)

        return flows


    def getJoanaFlowIDBySourceIdentifying(self, componentId: str, interfaceId:str, operationId: str) -> str:
        joanaFlow: List[dict] = self.getJoanaFlowsBySourceIdentifying(componentId, interfaceId, operationId)

        joanaFlowTags: List[str] = []

        if joanaFlow is not None:
            for flow in joanaFlow:
                joanaFlowTags.append(flow["joanaFlowId"])

        return joanaFlowTags





