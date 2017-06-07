package com.example.gps.app2_gps;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
    Main class of the application
        Monitors the location of the device and onLocationChanged is called based on set time and distance
        When the set time has elapsed, the onLocation Method is called
        The detected Bluetooth devices are stored
        Unique BT devices are added to one Firebase DB
        Co-Ordinates and BT devices of each location are uploaded to another list in the Firebase DB
 */
public class GPSService extends Service implements android.location.LocationListener {
    private LocationManager lm;
    private static final String TAG = "GPS_Service";
    private DatabaseReference mDatabase;                                                        //Location + Bluetooth Info Backend Server
    private Context mContext = this;
    private BluetoothAdapter mBtAdapter;
    private ArrayList<String> newUniqueDeviceInTest = new ArrayList<String>();                      //Stores any unique device found in a single BT scan
    public ArrayList<String> uniqueDevices = new ArrayList<String>();                               //Stores all unique devices found
    private ArrayList<String> locationDevices = new ArrayList<String>();                            //Stores all devices found in a single BT scan

    boolean isGPSEnabled = false;                                                               // flag for GPS status
    boolean isNetworkEnabled = false;                                                           // flag for network status
    private boolean gpsActive = false;
    private boolean networkActive = false;
    private Handler mHandler;
    private int mInterval = 600000; //1000*60*10                                                //Checking GPS/Network status every 10 minutes
    private boolean dbDownloadComplete = false;                                                 //Asserted true when all first database query completed
    private int TenMins = 600000; //1000*60*10;                                                 //Time used for updating the location



    public GPSService(){
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");

        return null;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        mDatabase = FirebaseDatabase.getInstance().getReference();                                      //Reference to Firebase backend

        setUpUniqueDeviceList();                                                                        //Generates an array list by downloading all unique BT devices discovered from Firebase

        try {

            Toast.makeText(getApplicationContext(), "GPS Service Started!", Toast.LENGTH_SHORT);        //Displays that Service has started

            lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);  //Initialise location manager


            /* Requesting location updates   */
            lm.requestLocationUpdates(lm.GPS_PROVIDER,TenMins ,0,this);          //Start using GPS - update every 20 sec
            //lm.requestLocationUpdates(lm.NETWORK_PROVIDER,TenMins, 0,this );   //Start using Network - update every 20 sec

            /**** Setup Bluetooth Receiver  ****/
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mBtReceiver, filter);
            /**** End of Bluetooth Setup ****/

            mHandler = new Handler();                                               //Handler for implementing the timer
            mStatusChecker.run();                                                   //Start the timer


        }
        catch (SecurityException e) {
            Log.e(TAG, "exception occured " + e.getMessage());
        }
        catch (Exception e) {
            Log.e(TAG, "exception occured " + e.getMessage());
        }
    }


    /* Timer used to periodically check on the GPS/Netowrk location services available */
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                //updateStatus(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
                checkGPSandNetworkStatus();                             //Periodically check on the GPS/Netowrk location services available
                System.out.println("Updating location type");
            }

        }
    };

    /* Checks the status of the GPS/Network location services available
    *       GPS is preferred over Network
    */
    public void checkGPSandNetworkStatus(){
        //Check Permissions have been accepted
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //Get status of GPS and Network services
            try{isGPSEnabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
            try{isNetworkEnabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

            //If GPS is now active having been unavailable use it
            if (isGPSEnabled && !gpsActive){
                lm.requestLocationUpdates(lm.GPS_PROVIDER,TenMins ,0,this);   //Start using GPS - update every 10 mins
                gpsActive = true;
                networkActive = false;
            }
            //Otherwise use Network Services if they are available
            else if (isNetworkEnabled && !networkActive){
                lm.requestLocationUpdates(lm.NETWORK_PROVIDER,TenMins ,0,this); //Start using GPS - update every 10 mins
                networkActive = true;
                gpsActive = false;
            }
        }
        else{
            System.out.println("Permissions not granted");
        }
    }


    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (lm != null) {
            try {
                lm.removeUpdates(this);
            }
            catch (SecurityException e) {
                Log.e(TAG, "exception occured " + e.getMessage());
            }
            catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();       //Turn off any ongoing discovery
        }
        unregisterReceiver(mBtReceiver);         //Remove bluetooth listener
        mHandler.removeCallbacks(mStatusChecker);//Turn of the timer
        System.out.println("service stopped!!!!!!!!!!!!!!!!!!!!!!!");
    }



    /* Called on a change of location of the device every 15 minutes */
    public void onLocationChanged(Location arg0) {
        System.out.println("Location Changed");
        getBluetoothDevices();                                          //Start searching for all bluetooth devices

        System.out.println("Discovering BT");
        while(mBtAdapter.isDiscovering()){}                               // Time required to find all devices, wait until finished searching
        System.out.println("fINSIHED Discovering BT");

        /* Upload the co-ordinates and the devices discovered to Firebase */
        LocationData obj = new LocationData(arg0.getLatitude(),arg0.getLongitude(), locationDevices);   //Create the object to push, stores co-ords and list of BT devices
        mDatabase = FirebaseDatabase.getInstance().getReference();                                      //Reference to address to store data
        mDatabase.child("Blue+GPS").push().setValue(obj);                                               //Push object to correct list in the DB
        Log.e(TAG, "Data logged: " + obj.latitude + " " + obj.longitude);
        System.out.println("Data logged: " + obj.latitude + " " + obj.longitude);


        //Only need to add to the Firebase unique devices list if a new device has been identified in the latest BT scan
        if (!newUniqueDeviceInTest.isEmpty()) {                                     //List is not empty when a new unique device has been found
            int id = 1;                                                             //Used for naming the unique devices

            String DateTime = DateFormat.getDateTimeInstance().format(new Date()); //Generate the unique ID for uploading Unique BT devices
            DateTime = DateTime.replace(":", "-");
            DateTime = DateTime.replace(".", "_");

            ArrayList<String> dev = new ArrayList<String>();                            //Device must be in arrayList for correct format on Firebase

            //Loop over all newly discovered devices
            for(String uniqueDev : newUniqueDeviceInTest){
                dev.add(uniqueDev);                                                                                     //Array List ensures device is correctly displayed in DB
                mDatabase = FirebaseDatabase.getInstance().getReference().child("UniqueDevices").getRef();              //Reference to the backend location
                mDatabase.child(DateTime+" "+id).setValue(dev);                                                         //Pushing data to Firebase with an ID of date and time
                System.out.println("Unique BT device put to database");
                id++;                                                                                                   //Incrementing in case where multiple new devices found
                dev.clear();
            }

            newUniqueDeviceInTest.clear();  //Clear the list so its reset for next time
        }
        else{
            System.out.println("No unique devices found");
        }
        locationDevices.clear();            //Clear the list so its reset for next time

    }


    /* Called when want to search for nearby BT devices */
    public void getBluetoothDevices(){

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();          // Getting the Bluetooth adapter
        if(mBtAdapter != null) {
            mBtAdapter.startDiscovery();                            //Start looking for new devices
            System.out.println("Started looking for BT device");
            Toast.makeText(this, "Starting discovery...", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Bluetooth disabled or not available.", Toast.LENGTH_SHORT).show();
        }
    }


    public void onProviderDisabled(String arg0) {
        Log.e(TAG, "provider disabled " + arg0);
    }

    public void onProviderEnabled(String arg0) {
        Log.e(TAG, "provider enabled " + arg0);
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        Log.e(TAG, "status changed to " + arg0 + " [" + arg1 + "]");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,"Background Service Started",Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG, "onStart");
    }

    /* Method called once at the start of the service
            Sets up the unique device list by downloading all results from firebase
            If any new devices are recognised while the app is active, they are added to this offline list as well as being uploaded
     */
    private void setUpUniqueDeviceList() {
        final DatabaseReference ref = mDatabase.child("UniqueDevices").getRef(); //Reference to address of Location Data
        System.out.println("set up map ref..." + ref);

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot deviceRef : dataSnapshot.getChildren()) {             //Loop over all unique device entries in the database
                    String BTdevice = deviceRef.getValue().toString();
                    System.out.println(BTdevice);

                    if (BTdevice != null) {
                        BTdevice = BTdevice.replaceAll("\\[", "").replaceAll("\\]","");
                        uniqueDevices.add(BTdevice);                                    //Add device to array list
                    }
                }
                System.out.println("DB DOWNLOAD COMPLETE");
                dbDownloadComplete = true;    //On true, db download complete so can now distinguish if any devices found in search have been seen before
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }

        });

    }

    //Called when a bluetooth discovery is finished
        //See mBtReceiver setup in onCreate - looks out for ACTION_DISCOVERY_FINISHED
    private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                locationDevices.add("Name: " + device.getName() + " Address: " + device.getAddress());  //Adds a detected device to the list

                String id = device.getName() + ", " + device.getAddress();                              //Store device in format [ name, address ]

                if (!uniqueDevices.contains(id) && dbDownloadComplete) {                                //Check if unique device list has been downloaded and if it contains the current device
                    uniqueDevices.add(device.getName() + ", " + device.getAddress());                   //Add it to the local unique device list
                    newUniqueDeviceInTest.add(device.getName() + ", " + device.getAddress());           //Adding to this list triggers it to be pushed to Firebase

                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, uniqueDevices);
            }
        }
    };



}
