package com.example.gheggie.gait;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth firebaseAuth;
    private EditText emailText;
    private EditText passwordText;
    private EditText confirmText;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MapActivity.class));
        }

        //change statusBar color
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

        // initialize UI elements
        Button registerButton = (Button)findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        emailText = (EditText)findViewById(R.id.emailText);
        passwordText = (EditText)findViewById(R.id.passwordText);
        confirmText =(EditText)findViewById(R.id.confirmPasswordText);
        progressDialog = new ProgressDialog(this);

        TextView loginButton = (TextView) findViewById(R.id.loginButton);

        //open login screen
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginFragment loginFrag = LoginFragment.newInstance();
                getFragmentManager().beginTransaction().replace(
                        R.id.loginFrame,
                        loginFrag,
                        LoginFragment.TAG
                ).commit();
            }
        });
    }

    @Override
    public void onClick(View v) {
        checkConnection();
    }

    // check connection
    private void checkConnection() {
        ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mgr != null) {
            NetworkInfo netInfo = mgr.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    // if this is true, run registration
                    userRegistration();
                }
            } else { // if there is no active connection
                Toast.makeText(
                        this, "Check connection",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void userRegistration() {
        String userEmail = emailText.getText().toString().trim();
        String userPassword = passwordText.getText().toString().trim();
        String confirmPassword = confirmText.getText().toString().trim();

        if (TextUtils.isEmpty(userEmail)) {// if email field is empty, notify user
            emailText.setError("Please enter an email");
        } else if (TextUtils.isEmpty(userPassword)) { // if password field is empty, notify user
            passwordText.setError("Please enter a password");
        }  else if (!userPassword.equals(confirmPassword)){
            passwordText.setText(null);
            confirmText.setText(null);
            Toast.makeText(this, "Passwords did not match", Toast.LENGTH_SHORT).show();
        }else {
            //show progress dialog
            progressDialog.setMessage("Registering User...");
            progressDialog.show();

            //check email
            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "Registration Unsuccessful. Try Again!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
