package com.example.gps.app2_gps;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class BluetoothListScreeen extends AppCompatActivity {

    private ListView listView;                                                  //ListView for displaying unique devices results
    private DatabaseReference mDatabase;                                        //Reference to Firebase backend
    public ArrayList<String> uniqueDevices = new ArrayList<String>();                //List for storing the unique devices
    private ArrayAdapter<String> adapter;                                       //array list adapter, used for dynamic updating

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list_screeen);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("UniqueDevices").getRef();  //Reference to relevant location in Firebase back end

        listView = (ListView) findViewById(R.id.uniqueBlueList);                        //Setting reference to list view in the layout file
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, uniqueDevices);   // initialising the adapter
        listView.setAdapter(adapter);                                                   //Assigning the adapter

        //Listens to changes in the "UniqueDevices" database
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                for (DataSnapshot deviceRef : dataSnapshot.getChildren()) {             //Loop over each entry in db
                    String BTdevice = deviceRef.getValue().toString();                  //Data for each BT device is stored as a string
                    System.out.println(BTdevice);

                    if (BTdevice != null) {
                        BTdevice = BTdevice.replaceAll("\\[", "").replaceAll("\\]",""); //Remove the square brackets
                        uniqueDevices.add(BTdevice);                                    //Add to the arrayList
                    }
                }
                adapter.notifyDataSetChanged();                                         //List has been updated to notify the listView
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        System.out.println("Devices found......");

    }

}
