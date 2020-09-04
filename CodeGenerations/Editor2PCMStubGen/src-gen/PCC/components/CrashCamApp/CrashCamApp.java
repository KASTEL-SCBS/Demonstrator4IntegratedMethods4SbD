package PCC.components.CrashCamApp;

import PCC.contracts.interfaces.IUploadingVideo;
import PCC.contracts.interfaces.IAuthenticatingCrashCamOperator_CrashCamApp;
import PCC.contracts.interfaces.IRecordingVideo;
import PCC.contracts.interfaces.ICryptographicHashFunction;
import PCC.contracts.interfaces.IHybridEncryption;
		
public class CrashCamApp implements IAuthenticatingCrashCamOperator_CrashCamApp, IRecordingVideo, IUploadingVideo {
	
	private IHybridEncryption iHybridEncryption;
	private ICryptographicHashFunction iCryptographicHashFunction;
	
	public CrashCamApp(IHybridEncryption iHybridEncryption, ICryptographicHashFunction iCryptographicHashFunction) {
		// TODO: implement and verify auto-generated constructor.
	    this.iHybridEncryption = iHybridEncryption;
	    this.iCryptographicHashFunction = iCryptographicHashFunction;
	}
	
	public void authenticatingCrashCamOperator_CrashCamApp(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
	@EntryPoint(tag="0#_Cr9r4G9SEeqa34g-QYI2IQ#_CsFAo29SEeqa34g-QYI2IQ") 
	@Source(tags="0#_Cr9r4G9SEeqa34g-QYI2IQ#_CsFAo29SEeqa34g-QYI2IQ")
	public void recordingVideo(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
	@EntryPoint(tag="2#_CsBWQ29SEeqa34g-QYI2IQ#_CsFAo29SEeqa34g-QYI2IQ") 
	@EntryPoint(tag="2#_CsBWQ29SEeqa34g-QYI2IQ#_CsGOwG9SEeqa34g-QYI2IQ") 
	@Source(tags={"2#_CsBWQ29SEeqa34g-QYI2IQ#_CsFAo29SEeqa34g-QYI2IQ", "2#_CsBWQ29SEeqa34g-QYI2IQ#_CsGOwG9SEeqa34g-QYI2IQ"})
	public void uploadingVideo(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
}