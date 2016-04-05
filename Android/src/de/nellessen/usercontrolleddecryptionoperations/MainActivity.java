package de.nellessen.usercontrolleddecryptionoperations;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.nfc.NfcManager;
import android.nfc.NfcAdapter;
import android.view.WindowManager;

public class MainActivity extends Activity{

	public static String Tag = "UserControlledDecryptionOperations";
	private UcdoHostApduService MUcdoHostApduService;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		checkNfcEnabled();

		MUcdoHostApduService = UcdoHostApduService.getInstance(this);
	}

	private boolean nfcEnabled(){
		NfcManager mNfcManager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
		NfcAdapter mNfcAdapter = mNfcManager.getDefaultAdapter();
		return (mNfcAdapter != null && mNfcAdapter.isEnabled());
	}

	private void checkNfcEnabled(){
		if(!nfcEnabled()){
			// make a text input dialog and show it
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Fehler");
			alert.setMessage("NFC nicht aktiviert. Bitte aktivieren Sie NFC, bevor Sie fortfahren.");
			alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					checkNfcEnabled();
				}
			});
			alert.show();
		}
	}

}
