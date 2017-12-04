package com.example.gheggie.gait;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment implements View.OnClickListener{

    public static final String TAG = "LoginFragment.TAG";
    private FirebaseAuth firebaseAuth;
    private EditText emailText;
    private EditText passwordText;
    private ProgressDialog progressDialog;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();

        emailText = (EditText) getActivity().findViewById(R.id.emailText2);
        passwordText = (EditText)getActivity().findViewById(R.id.passwordText2);
        progressDialog = new ProgressDialog(getActivity());
        Button registerButton = (Button)getActivity().findViewById(R.id.loginButton2);
        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        checkConnection();
    }

    private void userLogin() {
        String userEmail = emailText.getText().toString().trim();
        String userPassword = passwordText.getText().toString().trim();

        if (TextUtils.isEmpty(userEmail)) {// if email field is empty, notify user
            emailText.setError("Please enter an email");
        } else if (TextUtils.isEmpty(userPassword)) { // if password field is empty, notify user
            passwordText.setError("Please enter a password");
        }else {
        //show progress dialog
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        //check email
        firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            removeFragment();
                            startActivity(new Intent(getActivity(), MapActivity.class));
                        } else {
                            Toast.makeText(getActivity(),
                                    "Login Unsuccessful. Try Again!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    private void removeFragment(){
        getActivity().getFragmentManager().beginTransaction().
                remove(this).commit();
    }

    // check connection
    private void checkConnection() {
        ConnectivityManager mgr = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mgr != null) {
            NetworkInfo netInfo = mgr.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    // if this is true, run login
                    userLogin();
                }
            } else { // if there is no active connection
                Toast.makeText(
                        getActivity(), "Check connection",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
