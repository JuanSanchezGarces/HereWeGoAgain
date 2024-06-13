package com.example.idosos;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;



public class Reminder {
    private String title;
    private String date;
    private String time;
    private boolean[] daysOfWeek;
    private List<UUID> workRequestIds;

    public Reminder(String title, String date, String time, boolean[] daysOfWeek) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.daysOfWeek = daysOfWeek;
        this.workRequestIds = new ArrayList<>();
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
    public List<UUID> getWorkRequestIds() { return workRequestIds; }

    public void addWorkRequestId(UUID id) {
        workRequestIds.add(id);
    }
}
