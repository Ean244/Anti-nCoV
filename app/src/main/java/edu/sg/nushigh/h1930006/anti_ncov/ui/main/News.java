package edu.sg.nushigh.h1930006.anti_ncov.ui.main;

public class News {
    private final String title;
    private final String body;
    private final String date;

    public News(String title, String body, String date) {
        this.title = title;
        this.body = body;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }
}
