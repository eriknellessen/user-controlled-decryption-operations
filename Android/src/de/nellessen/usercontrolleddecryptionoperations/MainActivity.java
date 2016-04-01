package de.nellessen.usercontrolleddecryptionoperations;

import android.app.Activity;
import android.os.Bundle;

import com.licel.jcardsim.base.Simulator;
import com.licel.jcardsim.base.SimulatorRuntime;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;

import openpgpcard.OpenPGPApplet;

public class MainActivity extends Activity{

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 1. Create simulator
		Simulator simulator = new Simulator(new SimulatorRuntime());

		// 2. Install applet
		//See https://github.com/frankmorgner/vsmartcard/blob/master/ACardEmulator/app/src/main/java/com/vsmartcard/acardemulator/SimulatorService.java
		String appletAidString = "D2760001240102000000000000010000";
		AID appletAid = AIDUtil.create(appletAidString);
		try{
			byte[] aidAsBytes = hexStringToByteArray(appletAidString);
			byte[] installationParameters = new byte[appletAidString.length() + 1];
			installationParameters[0] = (byte) aidAsBytes.length;
			System.arraycopy(aidAsBytes, 0, installationParameters, 1, aidAsBytes.length);
			simulator.installApplet(appletAid, OpenPGPApplet.class, installationParameters, (short) 0, (byte) installationParameters.length);
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("Could not install the applet.");
			System.exit(-1);
		}

		// 3. Select applet
		simulator.selectApplet(appletAid);

		// 4. Send APDU
		byte[] commandAPDU = new byte [] {0x00, (byte) 0xF1, 0x00, 0x00};
		byte[] response = simulator.transmitCommand(commandAPDU);

		// 5. Check response status word
		System.out.println("Response: " + byteArrayToHexString(response));
	}

	//From http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

	//From http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String byteArrayToHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

}
