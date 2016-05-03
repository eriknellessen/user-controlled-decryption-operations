package de.nellessen.usercontrolleddecryptionoperations;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.licel.jcardsim.base.Simulator;
import com.licel.jcardsim.base.SimulatorRuntime;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;

import openpgpcard.OpenPGPApplet;
import javacard.framework.ISO7816;
import java.nio.ByteBuffer;
import javacard.framework.Util;
import java.util.Arrays;
import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UcdoHostApduService{

	Context MContext;
	Simulator MSimulator;

	private static final String SignatureAuthorizationRequest = "Compute signature?";
	private static final String DecryptionAuthorizationRequestBeginning = "Decrypt folder? Metadata: Path: ";
	byte [] MetaDataAsByteArray;
	boolean MetaDataComplete = true;
	boolean AcceptDecryption = false;
	private static final String HashAlgorithm = "SHA-256";

	//Class implemented as singleton, so it can be used by different activities
	private static UcdoHostApduService instance;

	private UcdoHostApduService(Context appContext) {
		//Create simulator, install applet and select it
		MSimulator = new Simulator(new SimulatorRuntime());

		String appletAidString = appContext.getResources().getString(R.string.ykneo_openpgpapplet_aid_long);
		AID appletAid = AIDUtil.create(appletAidString);
		try{
			byte[] aidAsBytes = Converting.hexStringToByteArray(appletAidString);
			byte[] installationParameters = new byte[appletAidString.length() + 1];
			installationParameters[0] = (byte) aidAsBytes.length;
			System.arraycopy(aidAsBytes, 0, installationParameters, 1, aidAsBytes.length);
			MSimulator.installApplet(appletAid, OpenPGPApplet.class, installationParameters, (short) 0, (byte) installationParameters.length);
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("Could not install the applet.");
			System.exit(-1);
		}

		MSimulator.selectApplet(appletAid);

		this.MContext = appContext;
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

	public byte [] processCommandApdu(byte [] apdu, Bundle extras) {
		Log.d(MainActivity.Tag, "Received APDU (" + apdu.length + " bytes): " + Converting.byteArrayToHexString(apdu));
		byte [] response;
		//Save meta data. According to ISO7816-4, 0x23 is not used.
		if(apdu[ISO7816.OFFSET_INS] == 0x23){
			response = saveMetaData(apdu);
		} else {
			response = MSimulator.transmitCommand(apdu);
			Log.d(MainActivity.Tag, "Applet responded APDU (" + response.length + " bytes): " + Converting.byteArrayToHexString(response));
		}

		// PERFORM SECURITY OPERATION
		//If that was the last APDU concerning a decryption/signature command
		if(apdu[ISO7816.OFFSET_CLA] == ISO7816.CLA_ISO7816 && apdu[ISO7816.OFFSET_INS] == (byte) 0x2A){
			boolean askForOkResult = false;
			short p1p2 = Util.makeShort(apdu[ISO7816.OFFSET_P1], apdu[ISO7816.OFFSET_P2]);
			// COMPUTE DIGITAL SIGNATURE
			if (p1p2 == (short) 0x9E9A) {
				askForOkResult = ((AskForOk) MContext).askForOk(SignatureAuthorizationRequest);
			}
			// DECIPHER
			else if (p1p2 == (short) 0x8086) {
				askForOkResult = AcceptDecryption;
				AcceptDecryption = false;
				if(!hashValueCorrect(response)){
					askForOkResult = false;
				}
				if(MetaDataComplete == false){
					askForOkResult = false;
				}
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

	void getUserDecision(){
		String metaDataAsString = new String("");
		try{
			metaDataAsString = new String(MetaDataAsByteArray, "US-ASCII");
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
			System.exit(1);
		}
		String DecryptionAuthorizationRequest = DecryptionAuthorizationRequestBeginning + metaDataAsString;
		AcceptDecryption = ((AskForOk) MContext).askForOk(DecryptionAuthorizationRequest);
	}

	byte [] saveMetaData(byte [] apdu){
		byte [] tempByteArray;
		if(MetaDataComplete == true){
			Log.d(MainActivity.Tag, "Creating new meta data array.");
			tempByteArray = new byte [apdu[ISO7816.OFFSET_LC]];
			MetaDataComplete = false;
		} else {
			tempByteArray = new byte [MetaDataAsByteArray.length + apdu[ISO7816.OFFSET_LC]];
		}
		System.arraycopy(apdu, ISO7816.OFFSET_CDATA, tempByteArray, tempByteArray.length - apdu[ISO7816.OFFSET_LC], apdu[ISO7816.OFFSET_LC]);
		MetaDataAsByteArray = tempByteArray;

		if(apdu[ISO7816.OFFSET_CLA] == ISO7816.CLA_ISO7816){
			MetaDataComplete = true;
			getUserDecision();
		}

		ByteBuffer statusWordBuffer = ByteBuffer.allocate(2);
		statusWordBuffer.putShort(ISO7816.SW_NO_ERROR);
		return statusWordBuffer.array();
	}

	boolean hashValueCorrect(byte [] responseApdu){
		//TODO: Check, if apdu has status word 0x9000
		//Calculate hash from the seen meta data
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance(HashAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}
		Log.d(MainActivity.Tag, "Calculating hash value from the following " + MetaDataAsByteArray.length + " bytes: " + Converting.byteArrayToHexString(MetaDataAsByteArray));
		md.update(MetaDataAsByteArray, 0, MetaDataAsByteArray.length);
		byte [] calculatedHash = md.digest();

		//Get decrypted hash value from apdu
		byte [] decryptedHash = new byte [calculatedHash.length];
		System.arraycopy(responseApdu, 0, decryptedHash, 0, calculatedHash.length);

		//Compare hashes
		Log.d(MainActivity.Tag, "Calculated Hash: " + Converting.byteArrayToHexString(calculatedHash));
		Log.d(MainActivity.Tag, "Decrypted Hash: " + Converting.byteArrayToHexString(decryptedHash));
		if(Arrays.equals(calculatedHash, decryptedHash)){
			return true;
		}

		return false;
	}

	public void onDeactivated(int reason) {
		return;
	}

}