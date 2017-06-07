package com.example.gps.app2_gps;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

public class DisplayScreen extends AppCompatActivity {

    private BluetoothAdapter mBtAdapter;                            //BT adapter used for checking if user has BT enabled
    private int REQUEST_ENABLE_BT = 1;                              //number required for BT enabled on device check
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        int PERMISSION_CODE_1 = 23;                     //Random number required for checking permissions
        if (Build.VERSION.SDK_INT >= 23)                //Must ask user for permission before calling location api on versions before Marshmallow
        {
            if (ActivityCompat.checkSelfPermission(DisplayScreen.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE_1); //Checking permissions within the context of this activity
            }
        }

        setupBluetooth();                              //Check if user has bluetooth enabled on device

        Intent service = new Intent(getApplicationContext(), GPSService.class);
        startService(service);                        //Starting the GPS Background service



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }




    //Handles the button clicks for the 2 buttons available on the main activity
    public void onButtonClick(View v) {
        if (v.getId() == R.id.GPSActivity) {
            Intent i = new Intent(DisplayScreen.this, MapsActivity.class);          //If GPS button clicked, open the map with markers
            startActivity(i);
        } else if (v.getId() == R.id.BluetoothActivity) {
            Intent i = new Intent(DisplayScreen.this, BluetoothListScreeen.class);  //If the BT List button is clicked, opend the activity with all unique BT devices
            startActivity(i);
        }
    }




    //Detects if device has bluetooth, and in case where it is turned off, prompts user to turn it on
    public void setupBluetooth(){
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter == null) {
            //Device doesn't support Bluetooth
        }
        if(!mBtAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);  //Pop-up appears on screen for user to enable bluetooth
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy(){
        super.onDestroy();
        stopService(new Intent(this, GPSService.class));
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();       //Turn off any ongoing discovery
        }
    }



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("DisplayScreen Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


}
