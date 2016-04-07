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
import javacard.framework.ISO7816;
import java.nio.ByteBuffer;
import javacard.framework.Util;
import java.io.UnsupportedEncodingException;

public class UcdoHostApduService extends HostApduService {

	Context context;
	Simulator simulator;

	private static final String SignatureAuthorizationRequest = "Compute signature?";
	private static final String DecryptionAuthorizationRequestBeginning = "Decrypt file? Metadata: ";

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

		//TODO: Show decrypted meta data
		// PERFORM SECURITY OPERATION
		if(apdu[ISO7816.OFFSET_INS] == (byte) 0x2A){
			boolean askForOkResult = false;
			short p1p2 = Util.makeShort(apdu[ISO7816.OFFSET_P1], apdu[ISO7816.OFFSET_P2]);
			// COMPUTE DIGITAL SIGNATURE
			if (p1p2 == (short) 0x9E9A) {
				askForOkResult = ((AskForOk) context).askForOk(SignatureAuthorizationRequest);
			}
			// DECIPHER
			else if (p1p2 == (short) 0x8086) {
				byte [] metaDataAsByteArray = new byte [response.length - 2];
				System.arraycopy(response, 0, metaDataAsByteArray, 0, response.length - 2);
				String metaDataAsString = new String("");
				try{
					metaDataAsString = new String(metaDataAsByteArray, "UTF-8");
				} catch (UnsupportedEncodingException e){
					e.printStackTrace();
					System.exit(1);
				}
				String DecryptionAuthorizationRequest = DecryptionAuthorizationRequestBeginning + metaDataAsByteArray;
				askForOkResult = ((AskForOk) context).askForOk(DecryptionAuthorizationRequest);
			}
			Log.d(MainActivity.Tag, "askForOkResult: " + askForOkResult);
			if(askForOkResult == false){
				ByteBuffer statusWordBuffer = ByteBuffer.allocate(2);
				statusWordBuffer.putShort(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
				response = statusWordBuffer.array();
			}
		}

		Log.d(MainActivity.Tag, "Sending APDU (" + response.length + " bytes): " + Converting.byteArrayToHexString(response));
		return response;
	}

	@Override
	public void onDeactivated(int reason) {
		return;
	}

}