package com.example.gheggie.gait;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText chatMsg;
    private ListView chatListView;
    private GaitInfo gaitUser;
    private DatabaseReference database;
    private final ArrayList<Message> mMessages = new ArrayList<>();
    private MessageBaseAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity);

        gaitUser = GaitUtils.loadGait(this);
        database = FirebaseDatabase.getInstance().getReference();

        setupToolBar();

        //change statusBar color
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

        //initializing UI
        FloatingActionButton sendMsg = (FloatingActionButton)findViewById(R.id.msg_fab);
        sendMsg.setOnClickListener(this);
        chatMsg = (EditText)findViewById(R.id.msg_text);
        chatListView = (ListView)findViewById(R.id.msg_list);
    }

    // set toolbar up
    private void setupToolBar() {
        Toolbar msgBar = (Toolbar) findViewById(R.id.msg_bar);
        msgBar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        setSupportActionBar(msgBar);
        msgBar.setNavigationIcon(R.drawable.arrow);
        msgBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void grabMessages(Map<String, Object> newMessages){
        //clear old messages out of array
        mMessages.clear();

        // add all messages to array
        for(Map.Entry<String, Object> msg : newMessages.entrySet()){
            Map mMessage = (Map)msg.getValue();
            mMessages.add(new Message(mMessage.get("message").toString() , mMessage.get("gaitID").toString()));
        }

        // update chat view
        refreshMessages();
    }

    // database listener for new messages
    private void receiveMessages(){
        database.child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                grabMessages((Map<String, Object>) dataSnapshot.getValue());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // refresh chat with array list
    private void refreshMessages(){
        messageAdapter = new MessageBaseAdapter(this, mMessages, gaitUser);
        chatListView.setAdapter(messageAdapter);
        messageAdapter.notifyDataSetChanged();
        autoScrollChatView();
    }

    // auto scroll chat view. will be called when new messages are added
    private void autoScrollChatView() {
        chatListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                chatListView.setSelection(messageAdapter.getCount());
            }
        });
    }

    // check connection
    private boolean checkConnection() {
        ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mgr != null) {
            NetworkInfo netInfo = mgr.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    // if this is true, run message feed
                    return true;
                }
            } else { // if there is no active connection
                Toast.makeText(
                        this, "Check connection",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }

    // send new msg to database and update chat view
    @Override
    public void onClick(View v) {
        if(checkConnection()) {
            if (chatMsg.getText().length() > 0) {
                Message chatMessage = new Message(chatMsg.getText().toString(), gaitUser.getGaitID());
                database.child("Messages").push().setValue(chatMessage);
                chatMsg.setText(null);
                receiveMessages();
            }
        }
    }
}
