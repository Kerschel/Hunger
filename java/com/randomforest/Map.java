package com.example.sachinrajkumar.randomforest;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.ClusterManager;

public class Map extends FragmentActivity implements OnMapReadyCallback {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    StorageReference storageReference =  FirebaseStorage.getInstance().getReference("images");

    private GoogleMap mMap;
    private ClusterManager<Person> mClusterManager;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent myIntent = new Intent(Map.this,MainActivity.class);
                    Map.this.startActivity(myIntent);
                    return true;


                case R.id.navigation_notifications:
//                    Intent myIntent = new Intent(MainActivity.this,Map.class);
//                    MainActivity.this.startActivity(myIntent);
                    return true;
            }
            return false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_globe);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mapFragment.getMapAsync(this);
        loadFirebase();
    }

    public void loadFirebase() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot point : dataSnapshot.getChildren()) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    point.getKey();
                    String latitude = point.child("lat").getValue().toString();
                    String longitude = point.child("lon").getValue().toString();
                    String condition = point.child("condition").getValue().toString();
                    String datetime = point.child("time").getValue().toString();
//                    storageReference.child("").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            // Got the download URL for 'users/me/profile.png'
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception exception) {
//                            // Handle any errors
//                        }
//                    });



                    System.out.println("Latitude "+ latitude);
                    mClusterManager.addItem(new Person(Double.valueOf(latitude),Double.valueOf(longitude),condition,datetime));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        setupMap(googleMap);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-26.167616, 28.079329), 10));

        mClusterManager = new ClusterManager<>(this, googleMap);

        googleMap.setOnCameraIdleListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        googleMap.setOnInfoWindowClickListener(mClusterManager);
        loadFirebase();
        mClusterManager.cluster();

    }

    private void addTree() {
        for (int i = 0; i < 3; i++) {
            mClusterManager.addItem(new Person(-26.187616, 28.079329, "PJ", "https://twitter.com/pjapplez"));
            mClusterManager.addItem(new Person(-26.207616, 28.079329, "PJ2", "https://twitter.com/pjapplez"));
            mClusterManager.addItem(new Person(-26.217616, 28.079329, "PJ3", "https://twitter.com/pjapplez"));
        }
    }

    protected void setupMap(GoogleMap mMap) {
        if (mMap != null) {
            return;
        }

        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        // permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

    }
}
