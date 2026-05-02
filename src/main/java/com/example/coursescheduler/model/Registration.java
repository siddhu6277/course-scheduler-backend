package com.example.coursescheduler.model;

public class Registration {
    public String id;
    public String studentId;
    public String courseId;
    public String registeredAt;
    public String status;

    public Registration() {
    }

    public Registration(String id, String studentId, String courseId, String registeredAt, String status) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.registeredAt = registeredAt;
        this.status = status;
    }
}
