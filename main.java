package com.sebekerga.canteencard;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Create NFC Adapter
    NfcAdapter nfcAdapter;

    //Create Shared Preferences
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        //Check for NFC Adapter available
        if (nfcAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device doesn't support NFC", Toast.LENGTH_LONG).show();
            //Error message
            finish();
        }
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "NFC isn't enabled on this device", Toast.LENGTH_LONG).show();
            finish();
        }
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(getApplicationContext(), byteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)), Toast.LENGTH_LONG).show();
            //Log Shared Preferences
            Log.i("Current NFC Tag", byteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)));
            Log.i("Saved NFC Tag", sharedPreferences.getString(getString(R.string.tag), getString(R.string.tag_default)));
            //Check for saved Tag
            if (sharedPreferences.getString(getString(R.string.tag), getString(R.string.tag_default)) == getString(R.string.tag_default)) {
                //Add Tag
                sharedPreferences.edit().putString(getString(R.string.tag), byteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)))
                        .apply();
            } else if (sharedPreferences.getString(getString(R.string.tag), getString(R.string.tag_default)).equals(byteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)))) {

                //Create and launch Browser with opened Canteen system website
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://billing.kengudetyam.ru/cabinet/#main"));
                startActivity(browserIntent);

                //Close app
                finish();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, nfcAdapter); 
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, nfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link BaseActivity} requesting to stop the foreground dispatch.
     * @param adapter  The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    //Convert byte[] to hex string
    private String byteArrayToHexString(byte[] array) {

        //Create array with symbols
        char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        //Create string that will be returned
        String string = "";

        //Iterate byte array
        for (int i = 0; i < array.length; i++) {

            //Get first num of byte and write it to "string"
            string += hex[Math.abs(array[i] / 16)];

            //Get first num of byte and write it to "string"
            string += hex[Math.abs(array[i] % 16)];

            //Add ':' symbol to "string" if it isn't last symbol
            if (i != array.length - 1)
                string += ":";

        }

        return string;
    }
}