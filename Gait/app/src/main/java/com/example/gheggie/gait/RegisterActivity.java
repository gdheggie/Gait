package com.example.gheggie.gait;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference myDatabaseRef;
    private EditText firstNameText;
    private EditText lastNameText;
    private CheckBox mGait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        myDatabaseRef = database.getReference();

        //change statusBar color
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

        firstNameText = (EditText)findViewById(R.id.firstName);
        lastNameText = (EditText)findViewById(R.id.lastName);
        EditText addressText = (EditText)findViewById(R.id.street_address);
        addressText.setVisibility(View.VISIBLE);
        EditText cityText = (EditText)findViewById(R.id.city_text);
        cityText.setVisibility(View.VISIBLE);
        EditText stateText = (EditText)findViewById(R.id.state_text);
        stateText.setVisibility(View.VISIBLE);
        Button saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
        mGait = (CheckBox)findViewById(R.id.gait_check);
    }

    public void onClick(View v) {
        checkConnection();
    }

    private void savePerson() {
        String fName = firstNameText.getText().toString().trim();
        String lName = lastNameText.getText().toString().trim();
        Boolean isGait = false;

        if(mGait.isChecked()) {
            isGait = true;
        }

        Person person = new Person(fName, lName, isGait);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user !=null) {
            GaitInfo newUser = new GaitInfo(user.getUid(), person.getFullName(), isGait);
            GaitUtils.savePerson(this, newUser);
            myDatabaseRef.child("Users").child(user.getUid()).push().setValue(person);
    }
        finish();
        startActivity(new Intent(this, MapActivity.class));
    }

    // check connection
    private void checkConnection() {
        ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mgr != null) {
            NetworkInfo netInfo = mgr.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    // if this is true, save person
                    savePerson();
                }
            } else { // if there is no active connection
                Toast.makeText(
                        this, "Check connection",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
