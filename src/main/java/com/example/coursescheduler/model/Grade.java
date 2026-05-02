package com.example.coursescheduler.model;

public class Grade {
    public String studentId;
    public String courseId;
    public String grade;
    public String instructor;
    public String date;

    public Grade() {
    }

    public Grade(String studentId, String courseId, String grade, String instructor, String date) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.grade = grade;
        this.instructor = instructor;
        this.date = date;
    }
}
