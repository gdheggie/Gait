package com.example.gheggie.gait;

import java.io.Serializable;

class Person implements Serializable{
    private final String firstName;
    private final String lastName;
    private final Boolean gait;

    Person(String _first, String _last, Boolean _gait) {
        firstName = _first;
        lastName = _last;
        gait = _gait;
    }

    String getFullName() {
        return firstName + ' ' + lastName;
    }
}
