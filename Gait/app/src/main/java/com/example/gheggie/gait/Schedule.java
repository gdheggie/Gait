package com.example.gheggie.gait;

import java.io.Serializable;

class Schedule implements Serializable {

    private final String who;
    private final String when;

    Schedule(String _who, String _when) {
        who = _who;
        when = _when;
    }

    @Override
    public String toString() {
        return who + " : " + when;
    }
}
