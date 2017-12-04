package com.example.gheggie.gait;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class GaitMapFragment extends MapFragment implements OnMapReadyCallback,
        GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener, LocationListener, GoogleMap.OnMapLongClickListener {

    public static final String TAG = "GaitMapFragment.TAG";
    private boolean mRequestingUpdates = false;
    private LocationManager mLocationManager;
    private Location current;
    private GoogleMap mMap;
    private FloatingActionMenu fam;
    private FloatingActionMenu gaitMenu;
    private FloatingActionButton fabSchedule, fabRequest, fabSignOut;
    private FloatingActionButton gaitSchedule, gaitOpen, gaitOut;
    private String alertID;
    static boolean openRequest = true;
    static public double clientLat;
    static public double clientLon;
    private GaitInfo gaitUser;
    private DatabaseReference database;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private Schedule newSchedule;

    public static GaitMapFragment newInstance() {
        return new GaitMapFragment();
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getMapAsync(this);

        // load user info
        gaitUser = GaitUtils.loadGait(getActivity());
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Get our location manager.
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && !mRequestingUpdates) {
            // Request location updates using 'this' as our LocationListener.
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    2000, 10.0f, this);
            mRequestingUpdates = false;

            // Get our last known location and check if it's a valid location.
            current = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        setupFABs();

        if(gaitUser.isaGait() && openRequest) {
            requestNotification();
            scheduleNotification();
        } else {
            gaitFound();
        }
    }

    // setting up correct fab menu based on client or Gait
    private void setupFABs(){
        if(!gaitUser.isaGait()) {
            fabSchedule = (FloatingActionButton) getActivity().findViewById(R.id.fab1);
            fabRequest = (FloatingActionButton) getActivity().findViewById(R.id.fab2);
            fabSignOut = (FloatingActionButton) getActivity().findViewById(R.id.fab3);
            fam = (FloatingActionMenu) getActivity().findViewById(R.id.fab_menu);
            fam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (fam.isOpened()) {
                        fam.close(true);
                    }
                }
            });
            fabSchedule.setOnClickListener(onButtonClick());
            fabRequest.setOnClickListener(onButtonClick());
            fabSignOut.setOnClickListener(onButtonClick());

        } else if(gaitUser.isaGait()) {
            gaitSchedule = (FloatingActionButton) getActivity().findViewById(R.id.gait1);
            gaitOpen = (FloatingActionButton) getActivity().findViewById(R.id.gait2);
            gaitOut = (FloatingActionButton) getActivity().findViewById(R.id.gait3);
            gaitMenu = (FloatingActionMenu) getActivity().findViewById(R.id.gait_menu);
            gaitMenu.setVisibility(View.VISIBLE);
            gaitMenu.setMenuButtonColorNormal(R.color.colorPrimary);
            gaitMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (gaitMenu.isOpened()) {
                        gaitMenu.close(true);
                    }
                }
            });
            gaitSchedule.setOnClickListener(gaitButtonClick());
            gaitOpen.setOnClickListener(gaitButtonClick());
            gaitOut.setOnClickListener(gaitButtonClick());
        }
    }

    // client FAB button method
    private View.OnClickListener onButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == fabRequest) {
                    // open request form
                    RequestFragment requestFrag = RequestFragment.newInstance();
                    getFragmentManager().beginTransaction().setCustomAnimations(
                            R.animator.slide_up,
                            R.animator.slide_down
                    ).replace(
                            R.id.main_frame,
                            requestFrag,
                            RequestFragment.TAG
                    ).commit();
                } else if (view == fabSchedule) {
                    // open schedule form
                    ScheduleFragment scheduleFrag = ScheduleFragment.newInstance();
                    getFragmentManager().beginTransaction().replace(
                            R.id.main_frame,
                            scheduleFrag,
                            ScheduleFragment.TAG
                    ).commit();
                } else if(view == fabSignOut){
                    firebaseAuth.signOut();
                    removeFragment();
                    getActivity().finish();
                }
                fam.close(true);
            }
        };
    }

    // Gait FAB button method
    private View.OnClickListener gaitButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == gaitOpen) {
                    if(!openRequest) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Turn On Job Alerts?")
                                .setNegativeButton("NO", null)
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        openRequest = true;
                                    }
                                }).show();
                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Turn Job Alerts Off?")
                                .setNegativeButton("NO", null)
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        openRequest = false;
                                    }
                                }).show();
                    }
                } else if (view == gaitSchedule) {
                    Intent scheduleIntent = new Intent(getActivity(), GaitSchedule.class);
                    startActivity(scheduleIntent);
                } else if(view == gaitOut){
                    firebaseAuth.signOut();
                    removeFragment();
                    getActivity().finish();
                }
                gaitMenu.close(true);
            }
        };
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMap.clear();
        getMapAsync(this);
        gaitUser = GaitUtils.loadGait(getActivity());
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(this);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
             mMap.setMyLocationEnabled(true);
        }

        if (current != null) {
            // get current location
            LatLng currentPosition = new LatLng(current.getLatitude(), current.getLongitude());
            CameraUpdate currentLocation = CameraUpdateFactory.newLatLngZoom(currentPosition, 18);
            mMap.animateCamera(currentLocation);
            clientLat = current.getLatitude();
            clientLon = current.getLatitude();

            // stop requesting current location
            mLocationManager.removeUpdates(this);
        } else {
            // if phone has no location
            LatLng testLoc = new LatLng(28.5383, -81.3792);
            CameraUpdate currentLocation = CameraUpdateFactory.newLatLngZoom(testLoc, 18);
            clientLat = testLoc.latitude;
            clientLon = testLoc.longitude;
            mMap.animateCamera(currentLocation);
        }
    }

    // request notification that fires off for the Gait
    private void requestNotification() {
        database = firebaseDatabase.getReference().child("Alerts");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()) {
                    HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getChildren().iterator().next().getValue();

                    // grab info and send it to alert method to show to the Gait
                    ClientAlert newAlert = new ClientAlert(map.get("photoUri"),map.get("title"), map.get("notifyText"), map.get("latitude"),
                            map.get("longitude"));
                    alertID = dataSnapshot.getChildren().iterator().next().getKey();
                    alertGait(newAlert);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // schedule notification that fires off for the Gait
    private void scheduleNotification() {
        database = firebaseDatabase.getReference().child("ScheduleAlert");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildren().iterator().hasNext()) {
                    HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getChildren().iterator().next().getValue();
                    if (dataSnapshot.getChildrenCount() > 0) {
                        ClientAlert newAlert = new ClientAlert(
                                map.get("title"), map.get("notifyText"), map.get("date"));

                        // split title string to get just the name of Client
                        String who = map.get("title");
                        String[] person = who.split("for");

                        // get schedule and its ID
                        newSchedule = new Schedule(person[1],map.get("date"));
                        alertID = dataSnapshot.getChildren().iterator().next().getKey();

                        new AlertDialog.Builder(getActivity())
                                .setTitle(newAlert.getTitle())
                                .setMessage(newAlert.getNotifyText() + " "
                                + newAlert.getDate())
                                .setNegativeButton("DENY", null)
                                .setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        database.child(alertID).removeValue();

                                        // save schedule request to the Gait's schedule
                                        GaitUtils.saveSchedule(getActivity(),newSchedule);
                                        Toast.makeText(getActivity()
                                                , R.string.added_toast
                                                , Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // if gait is found this function fires off for the client
    private void gaitFound(){
        database = firebaseDatabase.getReference().child("Connected");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()) {
                    if (dataSnapshot.getChildrenCount() > 0) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Gait Found!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        GaitJobFragment clientFrag = GaitJobFragment.newInstance();
                                        getActivity().getFragmentManager().beginTransaction().setCustomAnimations(
                                                R.animator.slide_up,
                                                R.animator.slide_down).replace(
                                                R.id.main_frame,
                                                clientFrag,
                                                GaitJobFragment.TAG
                                        ).commit();
                                    }
                                }).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // alert for the Gait when a new alert comes in
    private void alertGait(final ClientAlert clientNotify) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(clientNotify.getTitle());
        builder.setMessage(clientNotify.getNotifyText());
        builder.setNegativeButton("DENY", null);
        builder.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openRequest = false;
                database = firebaseDatabase.getReference().child("Alerts");
                database.child(alertID).removeValue();

                // send location to google maps
                Uri toClient = Uri.parse(
                        "google.navigation:q="
                                +clientNotify.getLatitude()
                                +','
                                +clientNotify.getLongitude());

                database = firebaseDatabase.getReference().child("Connected");
                database.push().setValue(gaitUser);

                // start intent for google maps
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, toClient);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

                // show new fragment
                GaitJobFragment gaitFrag = GaitJobFragment.newInstance();
                getActivity().getFragmentManager().beginTransaction().setCustomAnimations(
                        R.animator.slide_up,
                        R.animator.slide_down).replace(
                        R.id.main_frame,
                        gaitFrag,
                        GaitJobFragment.TAG
                ).commit();
            }
        }).show();
    }

    // takes fragment off the screen
    private void removeFragment(){
        getActivity().getFragmentManager().beginTransaction().
                remove(this).commit();
    }
}
