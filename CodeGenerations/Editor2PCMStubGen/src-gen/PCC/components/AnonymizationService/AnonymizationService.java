package PCC.components.AnonymizationService;

import PCC.contracts.interfaces.IAnonymizingVideo;
import PCC.contracts.interfaces.IExplicitlyGrantingAccessToRawVideo;
import PCC.contracts.interfaces.IDownloadingAnonymizedVideo;
import PCC.contracts.interfaces.IReceivingVideo;
import PCC.contracts.interfaces.IAccessControl;
import PCC.contracts.interfaces.IAsymmetricCryptoAuthenticationScheme;
import PCC.contracts.interfaces.IHybridEncryption;
import PCC.contracts.interfaces.IAuthenticatingProsecutionAuthority;
import PCC.contracts.interfaces.ICryptographicHashFunction;
import PCC.contracts.interfaces.IStoringAnonymizedVideo;
import PCC.contracts.interfaces.IAuthenticatingCrashCamOperator;
import PCC.contracts.interfaces.IPasswordAuthentication;
import PCC.contracts.interfaces.IDigitalSignature;
import PCC.contracts.interfaces.IStoringVideo;
		
public class AnonymizationService implements IExplicitlyGrantingAccessToRawVideo, IStoringVideo, IReceivingVideo, IStoringAnonymizedVideo, IDownloadingAnonymizedVideo, IAnonymizingVideo, IAuthenticatingCrashCamOperator, IAuthenticatingProsecutionAuthority {
	
	private IPasswordAuthentication iPasswordAuthentication;
	private IAccessControl iAccessControl;
	private IDigitalSignature iDigitalSignature;
	private IAsymmetricCryptoAuthenticationScheme iAsymmetricCryptoAuthenticationScheme;
	private IHybridEncryption iHybridEncryption;
	private ICryptographicHashFunction iCryptographicHashFunction;
	
	public AnonymizationService(IPasswordAuthentication iPasswordAuthentication, IAccessControl iAccessControl, IDigitalSignature iDigitalSignature, IAsymmetricCryptoAuthenticationScheme iAsymmetricCryptoAuthenticationScheme, IHybridEncryption iHybridEncryption, ICryptographicHashFunction iCryptographicHashFunction) {
		// TODO: implement and verify auto-generated constructor.
	    this.iPasswordAuthentication = iPasswordAuthentication;
	    this.iAccessControl = iAccessControl;
	    this.iDigitalSignature = iDigitalSignature;
	    this.iAsymmetricCryptoAuthenticationScheme = iAsymmetricCryptoAuthenticationScheme;
	    this.iHybridEncryption = iHybridEncryption;
	    this.iCryptographicHashFunction = iCryptographicHashFunction;
	}
	
	@EntryPoint(tag="4#_CsAIIW9SEeqa34g-QYI2IQ#_CsFAp29SEeqa34g-QYI2IQ") 
	@EntryPoint(tag="4#_CsAIIW9SEeqa34g-QYI2IQ#_CsGOyG9SEeqa34g-QYI2IQ") 
	@Source(tags={"4#_CsAIIW9SEeqa34g-QYI2IQ#_CsFAp29SEeqa34g-QYI2IQ", "4#_CsAIIW9SEeqa34g-QYI2IQ#_CsGOyG9SEeqa34g-QYI2IQ"})
	public void explicitlyGrantingAccessToRawVideo(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
	@EntryPoint(tag="7#_CsAvN29SEeqa34g-QYI2IQ#_CsFAo29SEeqa34g-QYI2IQ") 
	@EntryPoint(tag="7#_CsAvN29SEeqa34g-QYI2IQ#_CsGOyG9SEeqa34g-QYI2IQ") 
	@Source(tags={"7#_CsAvN29SEeqa34g-QYI2IQ#_CsFAo29SEeqa34g-QYI2IQ", "7#_CsAvN29SEeqa34g-QYI2IQ#_CsGOyG9SEeqa34g-QYI2IQ"})
	public void storingVideo(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
	@EntryPoint(tag="6#_CsAvNW9SEeqa34g-QYI2IQ#_CsFAo29SEeqa34g-QYI2IQ") 
	@EntryPoint(tag="6#_CsAvNW9SEeqa34g-QYI2IQ#_CsGOwG9SEeqa34g-QYI2IQ") 
	@Source(tags={"6#_CsAvNW9SEeqa34g-QYI2IQ#_CsFAo29SEeqa34g-QYI2IQ", "6#_CsAvNW9SEeqa34g-QYI2IQ#_CsGOwG9SEeqa34g-QYI2IQ"})
	public void receivingVideo(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
	@EntryPoint(tag="9#_CsAvO29SEeqa34g-QYI2IQ#_CsGOyG9SEeqa34g-QYI2IQ") 
	@Source(tags="9#_CsAvO29SEeqa34g-QYI2IQ#_CsGOyG9SEeqa34g-QYI2IQ")
	public void storingAnonymizedVideo(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
	@EntryPoint(tag="5#_CsAvM29SEeqa34g-QYI2IQ#_CsGOwG9SEeqa34g-QYI2IQ") 
	@EntryPoint(tag="5#_CsAvM29SEeqa34g-QYI2IQ#_CsGOyG9SEeqa34g-QYI2IQ") 
	@Source(tags={"5#_CsAvM29SEeqa34g-QYI2IQ#_CsGOwG9SEeqa34g-QYI2IQ", "5#_CsAvM29SEeqa34g-QYI2IQ#_CsGOyG9SEeqa34g-QYI2IQ"})
	public void downloadingAnonymizedVideo(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
	@EntryPoint(tag="8#_CsAvOW9SEeqa34g-QYI2IQ#_CsGOyG9SEeqa34g-QYI2IQ") 
	@Source(tags="8#_CsAvOW9SEeqa34g-QYI2IQ#_CsGOyG9SEeqa34g-QYI2IQ")
	public void anonymizingVideo(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
	@EntryPoint(tag="10#_CsBWQW9SEeqa34g-QYI2IQ#_CsEZlW9SEeqa34g-QYI2IQ") 
	@Source(tags="10#_CsBWQW9SEeqa34g-QYI2IQ#_CsEZlW9SEeqa34g-QYI2IQ")
	public void authenticatingCrashCamOperator(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
	@EntryPoint(tag="3#_CsAII29SEeqa34g-QYI2IQ#_CsGOxG9SEeqa34g-QYI2IQ") 
	@Source(tags="3#_CsAII29SEeqa34g-QYI2IQ#_CsGOxG9SEeqa34g-QYI2IQ")
	public void authenticatingProsecutionAuthority(){
		// TODO: implement and verify auto-generated method stub
		throw new UnsupportedOperationException("TODO: auto-generated method stub");
	}
	
}