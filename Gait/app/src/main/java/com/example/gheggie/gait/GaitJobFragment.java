package com.example.gheggie.gait;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GaitJobFragment extends Fragment implements View.OnClickListener{

    public static final String TAG = "GaitJobFragment.TAG";
    private DatabaseReference database;
    private Intent endJobIntent;

    public static GaitJobFragment newInstance() {
        return new GaitJobFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gait_job_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        GaitInfo gaitUser = GaitUtils.loadGait(getActivity());

        // initializing UI
        Button messageClient = (Button)getActivity().findViewById(R.id.msg_button);
        Button endJob = (Button)getActivity().findViewById(R.id.end_job);
        ImageView gaitImg = (ImageView)getActivity().findViewById(R.id.gait_pic);
        messageClient.setOnClickListener(this);
        endJob.setOnClickListener(this);

        if(!gaitUser.isaGait()) {
            gaitImg.setVisibility(View.VISIBLE);
            endJob.setVisibility(View.GONE);
            messageClient.setText(R.string.msg_gait);
            jobEnded();
        } else {
            gaitImg.setVisibility(View.GONE);
            endJob.setVisibility(View.VISIBLE);
        }

        endJobIntent = new Intent(getActivity(), ReviewActivity.class);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.msg_button) {
            // go to message screen
            Intent msgIntent = new Intent(getActivity(), MessageActivity.class);
            startActivity(msgIntent);
        } else {
            // fire completed listener
            database = FirebaseDatabase.getInstance().getReference().child("Completed");
            database.push().setValue("DONE");
            database.removeValue();

            //remove connected object in database
            database = FirebaseDatabase.getInstance().getReference().child("Connected");
            database.removeValue();

            // alert Gait that the job is completed
            new AlertDialog.Builder(getActivity())
                    .setTitle("Job Completed!")
                    .setMessage(R.string.proceed)
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GaitMapFragment.openRequest = true;
                            endFragment();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            endFragment();
                            startActivity(endJobIntent);
                        }
                    }).show();
        }

    }

    //remove fragment
    private void endFragment(){
        getActivity().getFragmentManager().beginTransaction().
                remove(this).commit();
    }

    // tell user the gait ended the job and pops up an alert
    private void jobEnded(){
        GaitMapFragment.openRequest = true;

        database = FirebaseDatabase.getInstance().getReference().child("Completed");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()) {
                    if(dataSnapshot.getChildrenCount() > 0) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.job_end)
                                .setMessage(R.string.proceed)
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        endFragment();
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        endFragment();
                                        startActivity(endJobIntent);
                                    }
                                })
                                .show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
