package de.nellessen.usercontrolleddecryptionoperations;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

public class ProcessCommandApduWrapper extends HostApduService{

	UcdoHostApduService MUcdoHostApduService;

	public ProcessCommandApduWrapper(){
		MUcdoHostApduService = UcdoHostApduService.getInstance(this);
	}

	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras){
		return MUcdoHostApduService.processCommandApdu(apdu, extras);
	}

	@Override
	public void onDeactivated(int reason){
		return;
	}

}