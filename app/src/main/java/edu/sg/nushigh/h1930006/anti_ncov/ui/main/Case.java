package edu.sg.nushigh.h1930006.anti_ncov.ui.main;

public class Case {
    private final String name;
    private final String desc;
    private final double latitude;
    private final double longitude;
    private final int category;

    public Case(String name, String desc, double latitude, double longitude, int category) {
        this.name = name;
        this.desc = desc;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getCategory() {
        return category;
    }
}
