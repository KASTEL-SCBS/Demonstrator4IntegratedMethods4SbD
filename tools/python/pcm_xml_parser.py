import xml
from dataclasses import dataclass
from typing import List
from enum import Enum
import xml.etree.ElementTree as ET
from util import abs_path, get_config

xsiType:str = '{http://www.w3.org/2001/XMLSchema-instance}type'
repositoryXMLS = '{http://palladiosimulator.org/PalladioComponentModel/Repository/5.2}Repository'

class PCMResourceTypes(Enum):
    REPOSITORY = 0
    SYSTEM = 1


@dataclass
class PCMResourceIdentifications:
    fileName: str
    path: str
    pcmResourceType: PCMResourceTypes
    containedTopLevelElementId: str = ""



#Rudimentary Parsing of PCM XML for Repository and System
@dataclass
class Entity:
    id: str
    name: str

    def print(self):
        print("id: %s; name: %s" % (self.id, self.name))

#Repository-Classes

@dataclass
class OperationSignature(Entity):

    def print(self):
        Entity.print(self)

@dataclass
class Interface(Entity):
    operationSignatures: List[OperationSignature]

    def print(self):
        Entity.print(self)
        print(*self.operationSignatures, sep='\n')

class RoleType(Enum):
    REQUIRED = 0
    PROVIDED = 1

@dataclass
class ReqProvRole(Entity):
    roleType: RoleType
    targetInterface: Interface

class SEFFActionType(Enum):
    START = 0
    STOP = 1
    EXTERNAL_CALL = 2

@dataclass
class SEFFStep:
    id: str
    actionType: SEFFActionType


@dataclass
class SEFFStartAction(SEFFStep):
    successor: SEFFStep

@dataclass
class SEFFStopAction(SEFFStep):
    predecessor: SEFFStep


@dataclass
class SEFFExternalCallAction(SEFFStep):
    predecessor: SEFFStep
    successor: SEFFStep
    calledService: OperationSignature
    calledRole: ReqProvRole



@dataclass
class SEFF:
    id: str
    targetSignature: OperationSignature
    steps: List[SEFFStep]



@dataclass
class Component(Entity):
    requiredInterfaceRoles: List[ReqProvRole]
    providedInterfaceRoles: List[ReqProvRole]
    seffs: List[SEFF]

@dataclass
class Repository(Entity):
    components: List[Component]
    interfaces: List[Interface]

#System-Classes

@dataclass
class AssemblyContext(Entity):
    encapsulatedComponent: Component

class ConnectorType(Enum):
    ASSEMBLY = 0
    PROVIDED_DELEGATION = 1
    REQUIRED_DELEGATION = 2

@dataclass
class Connector(Entity):
    type: ConnectorType

@dataclass
class DelegationConnector(Connector):
    outerRole: ReqProvRole
    assemblyContext: AssemblyContext
    innerRole: ReqProvRole

@dataclass
class AssemblyConnector(Connector):
    requiringAssemblyContext: AssemblyContext
    providingAssemblyContext: AssemblyContext
    providedRole: ReqProvRole
    requiredRole: ReqProvRole

@dataclass
class System(Entity):
        assemblyContexts: List[AssemblyContext]
        providedRoles: List[ReqProvRole]
        requiredRoles: List[ReqProvRole]
        connectors: List[Connector]

class PCM_XMLParser:
    repositories: List[Repository]
    repositoryResources: List[PCMResourceIdentifications]
    systems: List[System]
    systemResources: List[PCMResourceIdentifications]

    def __init__(self):
        self.repositories = []
        self.repositoryResources = []
        self.systems = []
        self.systemResources = []


    def parse(self, resources: List[PCMResourceIdentifications]):
        self.parseRepositories(resources)
        self.parseSystems(resources)
        print("Finished Parsing")

    def parseRepositories(self, resources: List[PCMResourceIdentifications]):

        for resource in resources:
            if resource.pcmResourceType == PCMResourceTypes.REPOSITORY:
                repositoryTree = ET.parse(str(abs_path(resource.path)))
                root = repositoryTree.getroot()
                repoId:str = ""
                repoName:str = ""

                for repo in root.iter(repositoryXMLS):
                    repoId = self.getAttributeOfXMLNode(repo, "id")
                    repoName = self.getAttributeOfXMLNode(repo, "entityName")
                    break

                repository: Repository = Repository(repoId, repoName, [],[])
                resource.containedTopLevelElementId = repository.id

                self.parseInterfaces(root, repository)
                self.parseComponents(root, repository)

                self.repositories.append(repository)
                self.repositoryResources.append(resource)

    def parseInterfaces(self, root, repository: Repository):
        for node in root.iter('interfaces__Repository'):
            id: str = node.attrib.get("id")
            name: str = node.attrib.get("entityName")
            interface: Interface = Interface(id, name, [])

            for opSignature in node.iter('signatures__OperationInterface'):
                opSigId = opSignature.attrib.get("id")
                opSigName = opSignature.attrib.get("entityName")

                signature: OperationSignature = OperationSignature(opSigId, opSigName)
                interface.operationSignatures.append(signature)

            repository.interfaces.append(interface)


    def parseComponents(self, root, repository: Repository):
        for node in root.iter('components__Repository'):
            componentId: str = node.attrib.get('id')
            componentName: str = node.attrib.get('entityName')

            component:Component = Component(componentId, componentName, [], [], [])

            for xmlProvidedRole in node.iter('providedRoles_InterfaceProvidingEntity'):
                provRoleId: str = self.getAttributeOfXMLNode(xmlProvidedRole, 'id')
                provRoleName: str = self.getAttributeOfXMLNode(xmlProvidedRole, 'entityName')
                interfaceId: str = xmlProvidedRole.attrib.get('providedInterface__OperationProvidedRole')
                provInterface: Interface = self.getInterfaceFromRepositoryById(id=interfaceId, repository=repository)
                providedRole: ReqProvRole = ReqProvRole(provRoleId, provRoleName, roleType=RoleType.PROVIDED, targetInterface=provInterface)
                component.providedInterfaceRoles.append(providedRole)

            for xmlRequiredRole in node.iter('requiredRoles_InterfaceRequiringEntity'):
                reqRoleId: str = self.getAttributeOfXMLNode(xmlRequiredRole, 'id')
                reqRoleName: str = self.getAttributeOfXMLNode(xmlRequiredRole, 'entityName')
                interfaceId: str = xmlRequiredRole.attrib.get('requiredInterface__OperationRequiredRole')
                reqInterface: Interface = self.getInterfaceFromRepositoryById(id=interfaceId, repository=repository)
                reqRole: ReqProvRole = ReqProvRole(reqRoleId, reqRoleName, roleType=RoleType.REQUIRED,
                                                            targetInterface=reqInterface)
                component.requiredInterfaceRoles.append(reqRole)

            for xmlSEFF in node.iter('serviceEffectSpecifications__BasicComponent'):
                seff = self.parseSEFF(component, xmlSEFF, repository=repository)
                component.seffs.append(seff)

            repository.components.append(component)

    def getInterfaceFromRepositoryById(self, id: str, repository:Repository) -> Interface:
        for interface in repository.interfaces:
            if interface.id == id:
                return interface

    def getOperationSignatureByIdForRepository(self, id: str, repository: Repository) -> OperationSignature:
        for interface in repository.interfaces:
            for operationSignature in interface.operationSignatures:
                if operationSignature.id == id:
                    return operationSignature

    def getAttributeOfXMLNode(self, node, attributeName: str) -> str:
        return node.attrib.get(attributeName)

    def getCorrectRoleFromRequiredRoles(self, component:Component, roleId: str) -> ReqProvRole:
        for requiredRole in component.requiredInterfaceRoles:
            if requiredRole.id == roleId:
                return requiredRole

    def parseSEFF(self, component:Component, xmlSEFF, repository:Repository) -> SEFF:
        seffId = xmlSEFF.attrib.get('id')
        seffDescribedServiceId = self.getAttributeOfXMLNode(xmlSEFF, "describedService__SEFF")
        seffOpSig: OperationSignature = self.getOperationSignatureByIdForRepository(id=seffDescribedServiceId, repository=repository)
        seff: SEFF = SEFF(seffId, seffOpSig, [])
        for xmlStep in xmlSEFF.iter('steps_Behaviour'):
            stepType: str = xmlStep.attrib.get(xsiType)
            stepId: str = xmlStep.attrib.get('id')

            if stepType == "seff:StartAction":
                successorId: str = self.getAttributeOfXMLNode(xmlStep, 'successor_AbstractAction')
                startStep: SEFFStartAction = SEFFStartAction(stepId, actionType=SEFFActionType.START,
                                                                 successor=successorId)
                seff.steps.append(startStep)

            elif stepType == "seff:StopAction":
                predecessorId: str = self.getAttributeOfXMLNode(xmlStep, 'predecessor_AbstractAction')
                stopStep: SEFFStopAction = SEFFStopAction(stepId, actionType=SEFFActionType.STOP,
                                                              predecessor=predecessorId)
                seff.steps.append(stopStep)
            elif stepType == "seff:ExternalCallAction":
                successorId: str = self.getAttributeOfXMLNode(xmlStep, 'successor_AbstractAction')
                predecessorId: str = self.getAttributeOfXMLNode(xmlStep, 'predecessor_AbstractAction')
                calledServiceId = self.getAttributeOfXMLNode(xmlStep, 'calledService_ExternalService')
                calledService = self.getOperationSignatureByIdForRepository(id=calledServiceId, repository=repository)
                externalServiceRoleId = self.getAttributeOfXMLNode(xmlStep, 'role_ExternalService')
                externalServiceRole = self.getCorrectRoleFromRequiredRoles(component, externalServiceRoleId)
                externalCallStep: SEFFExternalCallAction = SEFFExternalCallAction(id=stepId,
                                                                                      actionType=SEFFActionType.EXTERNAL_CALL,
                                                                                      successor=successorId,
                                                                                      predecessor=predecessorId,
                                                                                      calledRole=externalServiceRole,
                                                                                      calledService=calledService)
                seff.steps.append(externalCallStep)

        return seff


    def parseSystems(self, resources: List[PCMResourceIdentifications]):
        for resource in resources:
            if resource.pcmResourceType == PCMResourceTypes.SYSTEM:
                systemTree = ET.parse(str(abs_path(resource.path)))
                root = systemTree.getroot()
                systemId = self.getAttributeOfXMLNode(root, "id")
                systemName = self.getAttributeOfXMLNode(root, "entityName")
                resource.containedTopLevelElementId=systemId
                system:System = System(id= systemId, name= systemName, assemblyContexts=[], requiredRoles=[], providedRoles=[], connectors=[])

                self.parseAssemblyContexts(root=root, system=system)
                self.parseSystemInterfaceRoles(root=root, system=system)
                self.parseConnectors(node=root, system=system)

                self.systems.append(system)
                self.systemResources.append(resource)



    def parseAssemblyContexts(self, root, system:System):
        for xmlAssemblyContext in root.iter('assemblyContexts__ComposedStructure'):
            assemblyContextId = self.getAttributeOfXMLNode(xmlAssemblyContext, "id");
            assemblyContextName = self.getAttributeOfXMLNode(xmlAssemblyContext, "entityName");
            componentHref:str = ""

            for encComp in xmlAssemblyContext.iter('encapsulatedComponent__AssemblyContext'):
                componentHref = self.getAttributeOfXMLNode(encComp, "href");
                break

            splitHref: List[str] = componentHref.split("#")
            repositoryFileName: str = splitHref[0]
            appliedRepository: Repository = self.findRepositoryForThroughResourceByResourceName(repositoryFileName)
            encapsulatedComponentId: str = splitHref[1]
            encapsulatedComponent:Component = self.findComponentInRepositoryById(appliedRepository, encapsulatedComponentId)
            assemblyContext: AssemblyContext = AssemblyContext(assemblyContextId, assemblyContextName, encapsulatedComponent)
            system.assemblyContexts.append(assemblyContext)


    def parseSystemInterfaceRoles(self, root, system:System):
        for xmlProvidedRole in root.iter('providedRoles_InterfaceProvidingEntity'):
            providedRoleId = self.getAttributeOfXMLNode(xmlProvidedRole, "id")
            providedRoleName = self.getAttributeOfXMLNode(xmlProvidedRole, "entityName")
            interfaceHref: str = self.getAttributeOfXMLNode(xmlProvidedRole[0], "href")
            splitHref: List[str] = interfaceHref.split("#")
            repositoryFileName: str = splitHref[0]
            repositoryOfInterface: Repository = self.findRepositoryForThroughResourceByResourceName(repositoryFileName)
            interfaceId: str = splitHref[1]
            interface = self.getInterfaceFromRepositoryById(interfaceId, repositoryOfInterface)
            provRole: ReqProvRole = ReqProvRole(id=providedRoleId, name=providedRoleName, targetInterface=interface, roleType=RoleType.PROVIDED)
            system.providedRoles.append(provRole)
        for xmlRequiredRole in root.iter('requiredRoles_InterfaceRequiringEntity'):
            requiredRoleId = self.getAttributeOfXMLNode(xmlProvidedRole, "id")
            requiredRoleName = self.getAttributeOfXMLNode(xmlProvidedRole, "entityName")
            interfaceHref: str = self.getAttributeOfXMLNode(xmlProvidedRole[0], "href")
            splitHref: List[str] = interfaceHref.split("#")
            repositoryFileName: str = splitHref[0]
            repositoryOfInterface: Repository = self.findRepositoryForThroughResourceByResourceName(
                    repositoryFileName)
            interfaceId: str = splitHref[1]
            interface = self.getInterfaceFromRepositoryById(interfaceId, repositoryOfInterface)
            requiredRole: ReqProvRole = ReqProvRole(id=requiredRoleId, name=requiredRoleName, targetInterface=interface, roleType=RoleType.REQUIRED)
            system.requiredRoles.append(requiredRole)


    def parseConnectors(self, node, system: System):
        for xmlConnector in node.iter('connectors__ComposedStructure'):
            xmlConnectorType: str = self.getAttributeOfXMLNode(xmlConnector, xsiType)
            connectorId: str = self.getAttributeOfXMLNode(xmlConnector, "id")
            connectorName: str = self.getAttributeOfXMLNode(xmlConnector, "entityName")

            if xmlConnectorType == "composition:AssemblyConnector":
                requiringAssemblyContextId: str = self.getAttributeOfXMLNode(xmlConnector,"requiringAssemblyContext_AssemblyConnector")
                providingAssemblyContextId: str = self.getAttributeOfXMLNode(xmlConnector,"providingAssemblyContext_AssemblyConnector")

                requiringAssemblyContext = self.getAssemblyContextInSystemById(requiringAssemblyContextId, system)
                providingAssemblyContext = self.getAssemblyContextInSystemById(providingAssemblyContextId, system)

                providedRoleHref:str = self.getAttributeOfXMLNode(xmlConnector[0], "href")
                requiredRoleHref:str = self.getAttributeOfXMLNode(xmlConnector[1], "href")

                splitProvidedHref: List[str] = providedRoleHref.split("#")
                splitRequiredHref: List[str] = requiredRoleHref.split("#")

                requiredRepository: Repository = self.findRepositoryForThroughResourceByResourceName(splitRequiredHref[0])
                providedRepository: Repository = self.findRepositoryForThroughResourceByResourceName(splitProvidedHref[0])

                requiredRole: ReqProvRole = self.findRoleWithIdInRepository(repository= requiredRepository, roleId=splitRequiredHref[1])
                providedRole: ReqProvRole = self.findRoleWithIdInRepository(repository= providedRepository, roleId=splitProvidedHref[1])

                assemblyConnector: AssemblyConnector = AssemblyConnector(id=connectorId, name=connectorName,type=ConnectorType.ASSEMBLY, requiringAssemblyContext=requiringAssemblyContext, providingAssemblyContext=providingAssemblyContext, providedRole=providedRole, requiredRole=requiredRole)
                system.connectors.append(assemblyConnector)

            elif xmlConnectorType == "composition:ProvidedDelegationConnector" or xmlConnectorType == "composition:RequiredDelegationConnector":
                outerRoleId: str = self.getAttributeOfXMLNode(xmlConnector, "outerProvidedRole_ProvidedDelegationConnector")
                role: ReqProvRole = None
                if xmlConnectorType == "composition:ProvidedDelegationConnector":
                    for providedRoleInSystem in system.providedRoles:
                        if providedRoleInSystem.id == outerRoleId:
                            role = providedRoleInSystem
                            break
                elif xmlConnectorType == "composition:RequiredDelegationConnector":
                    for requiredRoleInSystem in system.requiredRoles:
                        if requiredRoleInSystem.id == outerRoleId:
                            role = requiredRoleInSystem
                            break


                assemblyContextId: str = self.getAttributeOfXMLNode(xmlConnector, "assemblyContext_ProvidedDelegationConnector")
                assemblyContext: AssemblyContext = self.getAssemblyContextInSystemById(assemblyContextId, system=system)

                innerRoleHref: str = self.getAttributeOfXMLNode(xmlConnector[0], "href")
                splitinnerRole: List[str] = innerRoleHref.split("#")

                innerRoleRepository: Repository = self.findRepositoryForThroughResourceByResourceName(splitinnerRole[0])
                innerRole: ReqProvRole = self.findRoleWithIdInRepository(roleId=splitinnerRole[1],repository=innerRoleRepository)

                delegatorType: ConnectorType = None

                if xmlConnectorType == "composition:ProvidedDelegationConnector":
                    delegatorType = ConnectorType.PROVIDED_DELEGATION
                elif xmlConnectorType == "composition:RequiredDelegationConnector":
                    delegatorType = ConnectorType.REQUIRED_DELEGATION

                delegateConnector: DelegationConnector = DelegationConnector(id=connectorId, name=connectorName,type= delegatorType, outerRole=role, assemblyContext=assemblyContext, innerRole=innerRole)

                system.connectors.append(delegateConnector)





    def getAssemblyContextInSystemById(self, id:str, system:System ) -> AssemblyContext:
        for context in system.assemblyContexts:
            if context.id == id:
                return context

    def findRepositoryForThroughResourceByResourceName(self, resourceName: str) -> Repository:
        for resourceIdentification in self.repositoryResources:
            if resourceName == resourceIdentification.fileName:
                for repository in self.repositories:
                    if repository.id == resourceIdentification.containedTopLevelElementId:
                        return repository

    def findComponentInRepositoryById(self, repository: Repository, id: str):
        for component in repository.components:
            if component.id == id:
                return component

    def findRoleWithIdInRepository(self, roleId:str, repository:Repository):
        for component in repository.components:
            for providedRole in component.providedInterfaceRoles:
                if providedRole.id == roleId:
                    return providedRole

            for requiredRole in component.requiredInterfaceRoles:
                if requiredRole.id == roleId:
                    return requiredRole

    def interfaceAlreadyContained(self, repository:Repository, interfaceId: str) -> bool:
        interface = self.getInterfaceFromRepositoryById(interfaceId, repository)
        return interface is not None

    def getComponentById(self, id:str):
        for repository in self.repositories:
            for component in repository.components:
                if component.id == id:
                    return component

    def getInterfaceById(self, id:str):
        for repository in self.repositories:
            for interface in repository.interfaces:
                if interface.id == id:
                    return interface

    def getOperationSignatureById(self, id:str):
        for repository in self.repositories:
            for interface in repository.interfaces:
                for operationSignature in interface.operationSignatures:
                    if operationSignature.id == id:
                        return operationSignature

if __name__ == '__main__':

    pccRepositoryResource: PCMResourceIdentifications = PCMResourceIdentifications(fileName="PCC.repository", path="PCC-Models/PCM-Model-Complete/PCC.repository", pcmResourceType=PCMResourceTypes.REPOSITORY)
    pccSystemResource: PCMResourceIdentifications = PCMResourceIdentifications(fileName="TotalSystem.system", path="PCC-Models/PCM-Model-Complete/TotalSystem.system", pcmResourceType=PCMResourceTypes.SYSTEM)

    resources = [pccRepositoryResource, pccSystemResource]


    PCM_XMLParser().parse(resources)
