package com.example.gheggie.gait;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleFragment extends Fragment implements View.OnClickListener{

    public static final String TAG = "ScheduleFragment.TAG";
    private CheckBox walkerBox;
    private CheckBox sitterBox;
    private TextView fromText;
    private TextView toText;
    private Calendar myCalendar;
    private ClientAlert newClient;
    private Boolean walker = false;
    private Boolean sitter = false;
    private FirebaseDatabase database;
    private DatabaseReference alertRef;

    public static ScheduleFragment newInstance() {
        return new ScheduleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        alertRef = database.getReference();

        walkerBox = (CheckBox)getActivity().findViewById(R.id.walker_check2);
        sitterBox = (CheckBox)getActivity().findViewById(R.id.sitter_check2);
        Button confirmButton = (Button) getActivity().findViewById(R.id.confirm_request2);
        confirmButton.setOnClickListener(this);
        Button cancelButton = (Button) getActivity().findViewById(R.id.cancel_request2);
        cancelButton.setOnClickListener(this);
        fromText = (TextView)getActivity().findViewById(R.id.from_text);
        toText = (TextView)getActivity().findViewById(R.id.to_text);
        myCalendar = Calendar.getInstance();

        // set on click listeners for text fields
        fromText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), fromDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        toText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), toDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    // date picker for from text field
    private final DatePickerDialog.OnDateSetListener fromDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "MM/dd/yy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            fromText.setText(sdf.format(myCalendar.getTime()));

        }
    };

    // date picker for to text field
    private final DatePickerDialog.OnDateSetListener toDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "MM-dd-yy-hh-mm-ss"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            toText.setText(sdf.format(myCalendar.getTime()));
        }
    };

    @Override
    public void onClick(View v) {
        if(checkConnection()) {
            if (v.getId() == R.id.confirm_request2) {
                createSchedule();
            } else {
                removeFragment();
            }
        }
    }

    // create schedule request
    private void createSchedule(){
        if (walkerBox.isChecked()) {
            walker = true;
        }
        if (sitterBox.isChecked()) {
            sitter = true;
        }
        // setup notification for gaits
        GaitInfo gaitUser = GaitUtils.loadGait(getActivity());

        String fromDate = fromText.getText().toString();
        String toDate = toText.getText().toString();

        if (walker && sitter) {
            newClient = new ClientAlert("Future Job for " + gaitUser.getName(),
                    "Dog walker and sitter needed", fromDate + " to " + toDate);
        } else if (walker && !sitter) {
            newClient = new ClientAlert("Future Job for " + gaitUser.getName(),
                    "Dog walker needed", fromDate + " to " + toDate);
        } else if (sitter && !walker) {
            newClient = new ClientAlert("Future Job for " + gaitUser.getName(),
                    "Dog sitter needed", fromDate + " to " + toDate);
        }

        if (newClient != null) {
            alertRef = database.getReference().child("ScheduleAlert");
            alertRef.push().setValue(newClient);
            removeFragment();
        }
    }

    // remove fragment
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
                    // if this is true, send schedule request
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
