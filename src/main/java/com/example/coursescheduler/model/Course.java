package com.example.coursescheduler.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    public String id;
    public String name;
    public String description;
    public String instructor;
    public int credits;
    public List<String> days = new ArrayList<>();
    public String startTime;
    public String endTime;
    public String location;
    public int capacity;
    public int enrolled;
    public String semester;

    public Course() {
    }

    public Course(String id, String name, String description, String instructor, int credits, List<String> days, String startTime, String endTime, String location, int capacity, int enrolled, String semester) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.instructor = instructor;
        this.credits = credits;
        this.days = days;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.capacity = capacity;
        this.enrolled = enrolled;
        this.semester = semester;
    }
}
