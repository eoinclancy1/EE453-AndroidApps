package com.example.gps.app2_gps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.google.android.gms.maps.MapView;
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

    private LocationManager lm;
    private FileUtility myFile;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private DatabaseReference mDatabase;
    Marker now;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        int PERMISSION_CODE_1 = 23;                                         //Constant used when user does not allow GPS

        try {
            //App 2 todo: request updates here
            if (Build.VERSION.SDK_INT >= 23)             //Must ask user for permission before calling location api on versions before Marshmallow
            {

                if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {  ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE_1);
                }
            }
            /* Requesting location updates   */
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,5,this);          //Using GPS
            //lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000,5,this);    //Suggested for eng building -


        } catch (SecurityException e) {
            Log.e("GPS", "exception occured " + e.getMessage());
        } catch (Exception e) {
            Log.e("GPS", "exception occured " + e.getMessage());
        }

        setUpMapIfNeeded();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
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
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        final DatabaseReference ref = mDatabase.child("Locations/UserLocations").getRef(); //Reference to address of Location Data
        System.out.println("set up map ref..." + ref);

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot locSnapshot : dataSnapshot.getChildren()) {
                    LocationData loc = locSnapshot.getValue(LocationData.class);
                    //System.out.println(post);

                    if (loc != null) {
                        // App 2: Todo: Add a map marker here based on the loc downloaded
                        double latitude = loc.latitude;                                 //Get LAT from object read in
                        double longitude = loc.longitude;                               //Get LON from object read in
                        LatLng latLng = new LatLng(latitude, longitude);                //Combine to form LatLand object
                        now = mMap.addMarker(new MarkerOptions().position(latLng));     //Add marker to map based on object created
                    }
                }
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        //App 2  todo: center and zoom the map here

        LatLng coordinate = new LatLng(53.283912, -9.063874);                           //Co-ords for Engineering Building NUIG
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);  //Zooms to co-ords
        mMap.animateCamera(yourLocation);                                               //Animates the camera zoom

    }


    public void onLocationChanged(Location arg0) {
        //System.out.println("^^^^^^^Location Changed^^^^^^^^^^^^^^");
        //App 2  todo: add marker to map here
       /* if(now != null){            //Use to delete the current marker so that only one is shown at a time
            now.remove();
        }*/

        double latitude = arg0.getLatitude();                       //Get LAT from object read in
        double longitude = arg0.getLongitude();                     //Get LON from object read in
        LatLng latLng = new LatLng(latitude, longitude);            //Combine to form LatLng object
        now = mMap.addMarker(new MarkerOptions().position(latLng)); //Add the new co-ord to the map


        //App 2  todo: upload location to Firebase
        LocationData update = new LocationData(latitude,longitude);             //Object to upload
        mDatabase = FirebaseDatabase.getInstance().getReference("Locations");   //Reference to address to store data
        DatabaseReference locationRef = mDatabase.child("UserLocations");       //Creates reference to the child
        DatabaseReference newLocationRef = locationRef.push();                  //Creates a unique timestamp at the node
        newLocationRef.setValue(update);                                        //Store the new location object

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
