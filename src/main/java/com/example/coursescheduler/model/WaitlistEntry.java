package com.example.coursescheduler.model;

public class WaitlistEntry {
    public String id;
    public String studentId;
    public String courseId;
    public int position;
    public String date;

    public WaitlistEntry() {
    }

    public WaitlistEntry(String id, String studentId, String courseId, int position, String date) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.position = position;
        this.date = date;
    }
}
