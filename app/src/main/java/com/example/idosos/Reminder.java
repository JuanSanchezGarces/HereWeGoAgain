package com.example.idosos;

import java.util.Arrays;

public class Reminder {
    private String title;
    private String date;
    private String time;
    private boolean[] daysOfWeek;

    public Reminder(String title, String date, String time, boolean[] daysOfWeek) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.daysOfWeek = daysOfWeek;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public boolean[] getDaysOfWeek() {
        return daysOfWeek;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", daysOfWeek=" + Arrays.toString(daysOfWeek) +
                '}';
    }
}
