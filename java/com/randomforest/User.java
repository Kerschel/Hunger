package com.example.sachinrajkumar.randomforest;

/**
 * Created by sachinrajkumar on 17/02/2018.
 */

public class User {

    public String condition;
    public String time;
    public double lat;
    public double lon;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String condition, String time, double lat, double lon) {
        this.condition = condition;
        this.time = time;
        this.lat = lat;
        this.lon = lon;
    }

}