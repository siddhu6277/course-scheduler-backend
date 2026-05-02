package com.example.coursescheduler.model;

public class Favorite {
    public String id;
    public String studentId;
    public String courseId;
    public String date;

    public Favorite() {
    }

    public Favorite(String id, String studentId, String courseId, String date) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.date = date;
    }
}
