package com.example.gheggie.gait;

import java.io.Serializable;

class GaitInfo implements Serializable {
    private String gaitID;
    private String name;
    private boolean aGait;

    GaitInfo(){

    }

    GaitInfo(String _gaitID, String _name, boolean _gait) {
        gaitID = _gaitID;
        name = _name;
        aGait = _gait;
    }

    String getGaitID() {
        return gaitID;
    }

    public String getName() {
        return name;
    }

    boolean isaGait() {
        return aGait;
    }
}
