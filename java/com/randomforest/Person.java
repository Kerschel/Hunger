package com.example.sachinrajkumar.randomforest;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by kersc on 2/18/2018.
 */

public class Person implements ClusterItem {

    private final LatLng mPosition;
    private String name;
    private String twitterHandle;

    public Person(double lat, double lng, String name, String twitterHandle) {
        this.name = name;
        this.twitterHandle = twitterHandle;
        mPosition = new LatLng(lat, lng);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSnippet() {
        return twitterHandle;
    }
}
