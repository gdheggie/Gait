package com.example.gheggie.gait;

import java.io.Serializable;

class ClientAlert implements Serializable{

    private String photoUri;
    private final String notifyText;
    private final String title;
    private String latitude;
    private String longitude;
    private String date;

    ClientAlert(String _title, String _notifyText, String _date) {
        title = _title;
        notifyText = _notifyText;
        date = _date;
    }

    ClientAlert(String _pic,String _title, String _notifyText, String _date) {
        photoUri = _pic;
        title = _title;
        notifyText = _notifyText;
        date = _date;
    }

    ClientAlert(String _photoUri, String _title, String _notifyText, String _latit, String _longit) {
        photoUri = _photoUri;
        title = _title;
        notifyText = _notifyText;
        latitude = _latit;
        longitude = _longit;
    }

    public String getTitle() {
        return title;
    }

    String getLatitude() {
        return latitude;
    }

    String getLongitude() {
        return longitude;
    }

    String getDate() {
        return date;
    }

    String getNotifyText() {
        return notifyText;
    }
}
