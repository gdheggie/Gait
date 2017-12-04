package com.example.gheggie.gait;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class GaitSchedule extends AppCompatActivity {
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        mListView = (ListView) findViewById(R.id.schedule_list);

        checkConnection();
        setupToolBar();

        //change statusBar color
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void setupToolBar(){
        Toolbar scheduleBar = (Toolbar)findViewById(R.id.schedule_bar);
        scheduleBar.setTitle(R.string.your_schedule);
        scheduleBar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorAccent));

        setSupportActionBar(scheduleBar);
        scheduleBar.setNavigationIcon(R.drawable.arrow);
        scheduleBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //populate schedule
    private void showSchedule() {
        ArrayList<Schedule> mSchedule = GaitUtils.loadSchedules(this);
        scheduleAdapter(mSchedule);
    }

    // check connection
    private void checkConnection() {
        ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mgr != null) {
            NetworkInfo netInfo = mgr.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    // if this is true, run schedule
                    showSchedule();
                }
            } else { // if there is no active connection
                Toast.makeText(
                        this, "Check connection",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // add adapter to listView and show schedule
    private void scheduleAdapter(ArrayList<Schedule> schedules){
        // reference to List View
        ArrayAdapter<Schedule> gaitSchedule = new ArrayAdapter<>(
                GaitSchedule.this,
                android.R.layout.simple_list_item_1,
                schedules);
        mListView.setAdapter(gaitSchedule);
        gaitSchedule.notifyDataSetChanged();
    }
}
