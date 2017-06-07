package com.example.gps.app2_gps;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.gps.app2_gps.R.id.map;


public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback {

    private GoogleMap mMap;                 //Might be null if Google Play services APK is not available.
    private DatabaseReference mDatabase;    //Reference to Firebase DB
    Marker now;                             //Marker for adding pointer on the map
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mDatabase = FirebaseDatabase.getInstance().getReference();          //Reference to Firebase backend
        int PERMISSION_CODE_1 = 23;                                         //Constant used when user does not allow GPS

        try {
            // Request updates here
            if (Build.VERSION.SDK_INT >= 23)             //Must ask user for permission before calling location api on versions before Marshmallow
            {

                if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {  ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE_1); //Checking permissions within the context of this activity
                }
            }


        } catch (SecurityException e) {
            Log.e("GPS", "exception occured " + e.getMessage());
        } catch (Exception e) {
            Log.e("GPS", "exception occured " + e.getMessage());
        }

        setUpMapIfNeeded();     //Ensures the map has not already been instantiated

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(map)).getMapAsync(this);
        }
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            setUpMap();
        }
    }

    /**
     * This is used to set up the map
     *  Enteries in the Firebase backend server are downloaded and the stored co-ordinates extracted
     *  Markers are added at the locations of the co-ordinates
     *  Each marker has a label that when touched, identifies the number of BT devices detected there
     */
    private void setUpMap() {
        final DatabaseReference ref = mDatabase.child("Blue+GPS").getRef(); //Reference to address of Location Data
        System.out.println("set up map ref..." + ref);

        // Attach a listener to read the data at our database reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loop over all database enteries
                for (DataSnapshot locSnapshot : dataSnapshot.getChildren()) {           //Loops over all data available in the database
                    LocationData loc = locSnapshot.getValue(LocationData.class);        //Getting the LocationData object from the db

                    if (loc != null) {
                        //Add a map marker here based on the loc downloaded
                        double latitude = loc.latitude;                                 //Get LAT from object read in
                        double longitude = loc.longitude;                               //Get LON from object read in
                        LatLng latLng = new LatLng(latitude, longitude);                //Combine to form LatLand object

                        //Add marker to map based at location specified by LatLang with label showing number of BT devices detected there
                        now = mMap.addMarker(new MarkerOptions().position(latLng).title(""+loc.getNumBTdevices()));
                        System.out.println(latitude + " , " + longitude);
                    }
                }
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        // Center and zoom the map
        LatLng coordinate = new LatLng(53.283912, -9.063874);                           //Co-ords for Engineering Building NUIG
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);  //Zooms to co-ords
        mMap.animateCamera(yourLocation);                                               //Animates the camera zoom

    }


    public void onLocationChanged(Location arg0) {
    }

    public void onProviderDisabled(String arg0) {
        Log.e("GPS", "provider disabled " + arg0);
    }

    public void onProviderEnabled(String arg0) {
        Log.e("GPS", "provider enabled " + arg0);
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        Log.e("GPS", "status changed to " + arg0 + " [" + arg1 + "]");
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
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
