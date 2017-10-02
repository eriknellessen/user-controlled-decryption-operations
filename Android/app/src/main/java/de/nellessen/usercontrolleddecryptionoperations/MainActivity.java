package de.nellessen.usercontrolleddecryptionoperations;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.nfc.NfcManager;
import android.nfc.NfcAdapter;
import android.view.WindowManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MainActivity extends Activity implements AskForOk{

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

	//Idea taken from http://stackoverflow.com/questions/17899328/this-handler-class-should-be-static-or-leaks-might-occur-com-test-test3-ui-main
	private static class HandlerClass extends Handler {

		public HandlerClass() {
		}

		@Override
		public void handleMessage(Message msg) {
			throw new RuntimeException();
		}

	};

	boolean askForOkResult;
	public boolean askForOk(String data){
		//Code taken from http://stackoverflow.com/questions/2028697/dialogs-alertdialogs-how-to-block-execution-while-dialog-is-up-net-style
		// make a handler that throws a runtime exception when a message is received
		final HandlerClass handler = new HandlerClass();

		// make a text input dialog and show it
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		alert.setTitle("Permission request");
		alert.setMessage(data);
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				askForOkResult = true;
				handler.sendMessage(handler.obtainMessage());
			}
		});
		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				askForOkResult = false;
				handler.sendMessage(handler.obtainMessage());
			}
		});
		alert.show();

		// loop till a runtime exception is triggered.
		try { Looper.loop(); }
		catch(RuntimeException e2) {}

		return askForOkResult;
	}

}
