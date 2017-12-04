package com.example.gheggie.gait;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class MessageBaseAdapter extends BaseAdapter {

    private final Context context;
    private ArrayList<Message> mMessages = new ArrayList<>();
    private GaitInfo gaitUser = new GaitInfo();

    MessageBaseAdapter(Context mContext, ArrayList<Message> _mMessages, GaitInfo gaitInfo){
        context = mContext;
        mMessages = _mMessages;
        gaitUser = gaitInfo;
    }

    @Override
    public int getCount() {
        if(mMessages!=null) {
            return mMessages.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if( mMessages!=null &&
                position < mMessages.size() &&
                position > -1) {
            return mMessages.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        int PPL_iD = 0x10203040;
        return PPL_iD + position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.msg_cell,parent,false);
        }
        // setup text fields
        TextView userText = (TextView)convertView.findViewById(R.id.msg_view2);
        TextView otherText = (TextView)convertView.findViewById(R.id.msg_view);

        Message msg = mMessages.get(position);

        // set text to certain field, based on ID
        if(gaitUser.getGaitID().equals(msg.getGaitID())) {
            userText.setText(msg.getMessage());
            otherText.setVisibility(View.GONE);
        } else {
            otherText.setText(msg.getMessage());
            userText.setVisibility(View.GONE);
        }

        return convertView;
    }
}
