package com.example.gheggie.gait;

class Message {

    private final String message;
    private final String gaitID;

    Message(String msg, String id){
        message = msg;
        gaitID = id;
    }

    String getMessage() {
        return message;
    }

    String getGaitID() {
        return gaitID;
    }
}
