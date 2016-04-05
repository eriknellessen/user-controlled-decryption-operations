package de.nellessen.usercontrolleddecryptionoperations;

import android.content.Context;
import android.os.Bundle;
import android.nfc.cardemulation.HostApduService;
import android.util.Log;

import com.licel.jcardsim.base.Simulator;
import com.licel.jcardsim.base.SimulatorRuntime;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;

import openpgpcard.OpenPGPApplet;

public class UcdoHostApduService extends HostApduService {

	Context context;
	Simulator simulator;

	//Class implemented as singleton, so it can be used by different activities
	private static UcdoHostApduService instance;

	private UcdoHostApduService(Context appContext) {
		//Create simulator, install applet and select it
		simulator = new Simulator(new SimulatorRuntime());

		String appletAidString = appContext.getResources().getString(R.string.ykneo_openpgpapplet_aid_long);
		AID appletAid = AIDUtil.create(appletAidString);
		try{
			byte[] aidAsBytes = Converting.hexStringToByteArray(appletAidString);
			byte[] installationParameters = new byte[appletAidString.length() + 1];
			installationParameters[0] = (byte) aidAsBytes.length;
			System.arraycopy(aidAsBytes, 0, installationParameters, 1, aidAsBytes.length);
			simulator.installApplet(appletAid, OpenPGPApplet.class, installationParameters, (short) 0, (byte) installationParameters.length);
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("Could not install the applet.");
			System.exit(-1);
		}

		simulator.selectApplet(appletAid);

		this.context = appContext;
	}

	public static UcdoHostApduService getInstance(Context appContext) {
		if (UcdoHostApduService.instance == null) {
			UcdoHostApduService.instance = new UcdoHostApduService(appContext);
		}
		return UcdoHostApduService.instance;
	}
	
	public static UcdoHostApduService getInstance() {
		return UcdoHostApduService.instance;
	}

	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
		Log.d(MainActivity.Tag, "Received APDU (" + apdu.length + " bytes): " + Converting.byteArrayToHexString(apdu));
		byte [] response = simulator.transmitCommand(apdu);
		Log.d(MainActivity.Tag, "Sending APDU (" + response.length + " bytes): " + Converting.byteArrayToHexString(response));
		return response;
	}

	@Override
	public void onDeactivated(int reason) {
		return;
	}

}