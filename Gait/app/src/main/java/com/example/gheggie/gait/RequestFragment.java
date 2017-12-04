package com.example.gheggie.gait;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RequestFragment extends Fragment implements View.OnClickListener{

    public static final String TAG = "RequestFragment.TAG";
    private static final int REQUEST_PICTURES = 0x011101;
    private CheckBox walkerBox;
    private CheckBox sitterBox;
    private Boolean walker = false;
    private Boolean sitter = false;
    private ClientAlert newClient;
    private FirebaseDatabase database;
    private LinearLayout linLay;
    private ImageView dogPic;
    private String picUri;

    public static RequestFragment newInstance() {
        return new RequestFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.request_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        database = FirebaseDatabase.getInstance();

        walkerBox = (CheckBox)getActivity().findViewById(R.id.walker_check);
        sitterBox = (CheckBox)getActivity().findViewById(R.id.sitter_check);

        Button confirmButton = (Button) getActivity().findViewById(R.id.confirm_request);
        confirmButton.setOnClickListener(this);
        Button cancelButton = (Button) getActivity().findViewById(R.id.cancel_request);
        cancelButton.setOnClickListener(this);
        dogPic = (ImageView)getActivity().findViewById(R.id.pic_of_dog);
        linLay = (LinearLayout)getActivity().findViewById(R.id.pic_layout);
        ImageButton picOfDog = (ImageButton)getActivity().findViewById(R.id.dog_pic);
        picOfDog.setOnClickListener(this);

    }

    // send request for the Gait to listen for
    @Override
    public void onClick(View v) {
        if(checkConnection()) {
            if (v.getId() == R.id.confirm_request) {
                createRequest();

                if (newClient != null) {
                    // setup notification for gaits
                    DatabaseReference myDatabaseRef = database.getReference().child("Alerts");
                    myDatabaseRef.push().setValue(newClient);
                    removeFragment();
                }
            } else if (v.getId() == R.id.dog_pic){
                getPermission();
            } else{
                removeFragment();
            }
        }
    }

    // create request based on what the user selects
    private void createRequest(){
        GaitInfo gaitUser = GaitUtils.loadGait(getActivity());

        // see what the user needs
        if (walkerBox.isChecked()) {
            walker = true;
        }
        if (sitterBox.isChecked()) {
            sitter = true;
        }

        // if no picture is added
        if(picUri == null) {
            if (walker && sitter) {
                newClient = new ClientAlert("Request for " + gaitUser.getName(), "Dog walker & sitter needed.",
                        String.valueOf(GaitMapFragment.clientLat), String.valueOf(GaitMapFragment.clientLon));
            } else if (walker && !sitter) {
                newClient = new ClientAlert("Request for " + gaitUser.getName(), "Dog walker needed.",
                        String.valueOf(GaitMapFragment.clientLat), String.valueOf(GaitMapFragment.clientLon));
            } else if (sitter && !walker) {
                newClient = new ClientAlert("Request for " + gaitUser.getName(), "Dog sitter needed.",
                        String.valueOf(GaitMapFragment.clientLat), String.valueOf(GaitMapFragment.clientLon));
            }
        } else { // if no picture is added
            if (walker && sitter) {
                newClient = new ClientAlert(picUri, "Request for " + gaitUser.getName(), "Dog walker & sitter needed.",
                        String.valueOf(GaitMapFragment.clientLat), String.valueOf(GaitMapFragment.clientLon));
            } else if (walker && !sitter) {
                newClient = new ClientAlert(picUri, "Request for " + gaitUser.getName(), "Dog walker needed.",
                        String.valueOf(GaitMapFragment.clientLat), String.valueOf(GaitMapFragment.clientLon));
            } else if (sitter && !walker) {
                newClient = new ClientAlert(picUri, "Request for " + gaitUser.getName(), "Dog sitter needed.",
                        String.valueOf(GaitMapFragment.clientLat), String.valueOf(GaitMapFragment.clientLon));
            }
        }
    }

    private void getPermission() {
        //get permission if we do not have them yet
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            // Request permissions if we don't have it.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PICTURES);
        } else {
            // TODO: Start Camera Roll
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto , 1);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // open camera roll
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if (requestCode == 0 || requestCode == 1) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;
                Uri selectedImage = data.getData();
                picUri = selectedImage.toString();

            }
        }
    }

    private void removeFragment() {
        getActivity().getFragmentManager().beginTransaction().
                remove(this).commit();
    }

    // check connection
    private boolean checkConnection() {
        ConnectivityManager mgr = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mgr != null) {
            NetworkInfo netInfo = mgr.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    // if this is true, send request
                    return true;
                }
            } else { // if there is no active connection
                Toast.makeText(
                        getActivity(), "Check connection",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }
}
