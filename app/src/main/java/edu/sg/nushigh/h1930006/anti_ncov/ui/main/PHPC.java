package edu.sg.nushigh.h1930006.anti_ncov.ui.main;

public class PHPC {
    private final String name;
    private final double latitude;
    private final double longitude;

    public PHPC(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
