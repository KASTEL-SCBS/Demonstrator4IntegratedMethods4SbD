
# Code to Mimic single analysis
import functools
import json
import logging
import subprocess
import sys

from util import get_config, open_path, abs_path, files, pprint
sys.path.append(str(abs_path(get_config().haskalladio_tool_folder)))
import xsb
import pcm_xml_parser
from pcm_xml_parser import PCM_XMLParser
from pcm_xml_parser import PCMResourceIdentifications
from pcm_xml_parser import PCMResourceTypes
from PCMFlow import PCMFlowAnalyzer
import PCMFlow
from xsb import Mode

import TrackingReader
from GoalModelingEditorCommunicator import GoalModelingEditorCommunicator
from joana import flows
from proofs import VerificationResults, VerificationResult, ProofResult

##
# ---Before Running the Script---
# Generate Goal-Model; Out: GoalModel <-- DONE
# Use KastelEditor2PCM to generate PCM Stubs and JOANA Model; In: GoalModel, Out: PCM Stubs + JOANA Flows from Goal Model  <-- DONE
# Refine PCM Model; In: PCM Stubs, Out: Complete PCM Model, Requirement: PCM Stubs preserved as "Facades" or "Servers/Clients" <-- DONE
# Generate Source Code Stubs from PCM & JOANA Model; In: PCM & JOANA Model, Out: Source Code Stubs with JOANA Annotations <-- DONE
# Fill Source Code Stubs; In: Source Code Stubs with JOANA Annotations, Out: Final Implementation <-- DONE
##

class Analysis:

    def __init__(self):
        self.trackingReader = TrackingReader.TrackingReader(json.load(open_path(get_config().tracking_file)),
                                                            abs_path(get_config().src_gen_folder))
        self.editor = GoalModelingEditorCommunicator(abs_path(get_config().editor_model_path), self.trackingReader)

    @functools.lru_cache()
    def proof(self) -> VerificationResults:
        pathToProofFiles = abs_path(get_config().proof_files_folder)

        res: VerificationResults = VerificationResults()
        proofFiles = files(pathToProofFiles)

        for appliedBBM in self.trackingReader.getAllAppliedBBMs():
            verificationResultsForBBM: VerificationResults = VerificationResults()
            proofFileCounterForBBM = 0
            for proofFile in proofFiles:

                proofPath = pathToProofFiles / proofFile

                if appliedBBM.replace(" ", "") in str(proofFile).replace(" ", ""):
                    proofFileCounterForBBM = proofFileCounterForBBM + 1
                    keyDirectoryPath = '../key'
                    keyRes = subprocess.call(['sh', keyDirectoryPath, keyDirectoryPath + '/key', proofPath])
                    if keyRes == 0:
                        verificationResultsForBBM.append(VerificationResult(appliedBBM, ProofResult.VERIFIED, proofFile))
                    else:
                        verificationResultsForBBM.append(VerificationResult(appliedBBM, ProofResult.FAILED, proofFile))

            if len(verificationResultsForBBM) == 0:
                res.append(VerificationResult(appliedBBM, ProofResult.NONEXISTENT))
            else:
                successCounter = 0
                for result in verificationResultsForBBM:
                    if result.is_verified():
                        successCounter += 1

                if successCounter == proofFileCounterForBBM:
                    res.append(VerificationResult(appliedBBM, ProofResult.VERIFIED))
                elif successCounter == 0:
                    res.append(VerificationResult(appliedBBM,  ProofResult.FAILED))
                else:
                    res.append(VerificationResult(appliedBBM, ProofResult.PARTIAL))

                for result in verificationResultsForBBM:
                    res.append(result)

        return res

    ## Coupling JOANA/KeY
    def couplingBase(self, verificationResults: VerificationResults):

        jflows = flows()

        for tag in self.trackingReader.getAllJoanaFlowTags():
            if tag not in jflows.results:
                logging.warning("Tag {} not analyzed with joana".format(tag))
                continue
            self.editor.annotate(tag, jflows.results[tag], verificationResults)

        self.editor.store()

    def run(self):
        results = self.proof()
        print(results)
        self.couplingBase(results)
        self.couplingAccessAnalysis()



##
# Access (False) Positive Check
# ForEach: Positive Unallowed Access for Adversary:
# 1. Precondition: Passive attacker & Adversary accesses signature/interface)
#    Negation: Flow from signature/interface includes Access Controlling BBM (Password Scheme, RBAC...)
#    Result: Unallowed Access avoided
# 2. Precondition: Active Attacker & Adversary accesses linking resource (LAN/WLAN)
#    Negation: Flow from signature/interface includes Encryption/Anonymization
#    Result: Unallowed Access avoided
##

    def couplingAccessAnalysis(self):
        subprocess.check_call(["make", get_config().haskalladio_analysis_type], cwd=str(abs_path(get_config().haskalladio_tool_folder)))
        accessAnalysisResults = xsb.parse(str(abs_path(get_config().haskalladio_tool_folder) / get_config().haskalladio_analysis_type))
        pccRepositoryResource: PCMResourceIdentifications = PCMResourceIdentifications(fileName="PCC.repository",
                                                                                       path="PCC-Models/PCM-Model-Complete/PCC.repository",
                                                                                       pcmResourceType=PCMResourceTypes.REPOSITORY)
        pccSystemResource: PCMResourceIdentifications = PCMResourceIdentifications(fileName="TotalSystem.system",
                                                                                   path="PCC-Models/PCM-Model-Complete/TotalSystem.system",
                                                                                   pcmResourceType=PCMResourceTypes.SYSTEM)



        resources = [pccRepositoryResource, pccSystemResource]
        pcmXMLParser:PCM_XMLParser = PCM_XMLParser()
        pcmXMLParser.parse(resources)
        pcmFlowAnalyzer:PCMFlowAnalyzer = PCMFlowAnalyzer(pcmXMLParser=pcmXMLParser, trackingReader=self.trackingReader)


        for insecure in accessAnalysisResults.proofs:

            if insecure.mode() == Mode.RETURN:
                startingComponent : str = pcmXMLParser.getComponentById(insecure.basicComponent).name
                startinInterface: str = pcmXMLParser.getInterfaceById(insecure.required).name
                startingOperationSig: str = pcmXMLParser.getOperationSignatureById(insecure.operationSignature).name

                bbmInFlow:bool = False

                bbmInFlow = pcmFlowAnalyzer.isSinkInFlow(sourceComponentId=insecure.basicComponent, sourceInterfaceId=insecure.required, sourceOperationId=insecure.operationSignature, sinkOperationId="_CsFAp29SEeqa34g-QYI2IQ")

                if bbmInFlow:
                    print("\n")
                    print(
                        "False Positive: Returnvalue for \n Adversary %s \n on Component: %s, Interface: %s, OperationSignature: %s on location: %s \n removed due to Call to Access-Control in Flow" % (
                        insecure.adversary, insecure.basicComponent, insecure.required, insecure.operationSignature, insecure.location))
                else:
                   print("\n")
                   print("True Positive for \n Adversary %s \n on Component: %s, Interface: %s, OperationSignature: %s on location: %s \n removed due to Call to Access-Control in Flow" % (insecure.adversary, insecure.basicComponent, insecure.required, insecure.operationSignature,insecure.location))

            else :
                print("True Positive for \n Adversary %s \n on Component: %s, Interface: %s, OperationSignature: %s on location: %s \n removed due to Call to Access-Control in Flow" % (insecure.adversary, insecure.basicComponent, insecure.required, insecure.operationSignature, insecure.location))





if __name__ == '__main__':
    Analysis().run()
