package com.example.coursescheduler.model;

public class Review {
    public String id;
    public String studentId;
    public String courseId;
    public int rating;
    public String comment;
    public String date;

    public Review() {
    }

    public Review(String id, String studentId, String courseId, int rating, String comment, String date) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
    }
}
