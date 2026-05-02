package com.example.coursescheduler.service;

import com.example.coursescheduler.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JsonDataService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path dataDir = Paths.get("data");

    @PostConstruct
    public void initData() throws IOException {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }

        ensureFile("courses.json", defaultCourses());
        ensureFile("users.json", defaultUsers());
        ensureFile("registrations.json", new ArrayList<>());
        ensureFile("grades.json", new ArrayList<>());
        ensureFile("reviews.json", new ArrayList<>());
        ensureFile("favorites.json", new ArrayList<>());
        ensureFile("waitlist.json", new ArrayList<>());
    }

    private void ensureFile(String fileName, Object initialData) throws IOException {
        Path file = dataDir.resolve(fileName);
        if (!Files.exists(file)) {
            Files.writeString(file, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(initialData));
        }
    }

    private <T> List<T> readList(String fileName, TypeReference<List<T>> typeReference) {
        Path file = dataDir.resolve(fileName);
        try {
            if (!Files.exists(file)) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file.toFile(), typeReference);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private <T> void writeList(String fileName, List<T> data) {
        Path file = dataDir.resolve(fileName);
        try {
            Files.writeString(file, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
        } catch (IOException ignored) {
        }
    }

    public List<Course> getCourses() {
        return readList("courses.json", new TypeReference<List<Course>>() {});
    }

    public void saveCourses(List<Course> courses) {
        writeList("courses.json", courses);
    }

    public List<User> getUsers() {
        return readList("users.json", new TypeReference<List<User>>() {});
    }

    public void saveUsers(List<User> users) {
        writeList("users.json", users);
    }

    public List<Registration> getRegistrations() {
        return readList("registrations.json", new TypeReference<List<Registration>>() {});
    }

    public void saveRegistrations(List<Registration> registrations) {
        writeList("registrations.json", registrations);
    }

    public List<Grade> getGrades() {
        return readList("grades.json", new TypeReference<List<Grade>>() {});
    }

    public void saveGrades(List<Grade> grades) {
        writeList("grades.json", grades);
    }

    public List<Review> getReviews() {
        return readList("reviews.json", new TypeReference<List<Review>>() {});
    }

    public void saveReviews(List<Review> reviews) {
        writeList("reviews.json", reviews);
    }

    public List<Favorite> getFavorites() {
        return readList("favorites.json", new TypeReference<List<Favorite>>() {});
    }

    public void saveFavorites(List<Favorite> favorites) {
        writeList("favorites.json", favorites);
    }

    public List<WaitlistEntry> getWaitlist() {
        return readList("waitlist.json", new TypeReference<List<WaitlistEntry>>() {});
    }

    public void saveWaitlist(List<WaitlistEntry> waitlist) {
        writeList("waitlist.json", waitlist);
    }

    private List<Course> defaultCourses() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("CS101", "Introduction to Computer Science", "Fundamentals of programming and computational thinking", "Dr. Sarah Smith", 3, List.of("Monday", "Wednesday", "Friday"), "09:00", "10:00", "Science Building 101", 30, 0, "Spring 2026"));
        courses.add(new Course("CS201", "Data Structures & Algorithms", "Advanced data structures and algorithm design", "Prof. James Johnson", 4, List.of("Tuesday", "Thursday"), "10:30", "11:45", "Engineering Hall 205", 25, 0, "Spring 2026"));
        courses.add(new Course("MATH101", "Calculus I", "Differential calculus, limits, and derivatives", "Dr. Maria Garcia", 4, List.of("Monday", "Wednesday", "Friday"), "10:00", "11:00", "Mathematics Building 301", 40, 0, "Spring 2026"));
        courses.add(new Course("MATH201", "Linear Algebra", "Matrices, vectors, and linear transformations", "Prof. Michael Chen", 3, List.of("Tuesday", "Thursday"), "14:00", "15:15", "Mathematics Building 402", 35, 0, "Spring 2026"));
        courses.add(new Course("ENG101", "English Composition", "Academic writing, critical thinking, and communication", "Dr. Emily Wilson", 3, List.of("Monday", "Wednesday", "Friday"), "13:00", "14:00", "Humanities Building 115", 20, 0, "Spring 2026"));
        courses.add(new Course("PHYS101", "Physics I: Mechanics", "Classical mechanics, motion, and forces", "Dr. Robert Brown", 4, List.of("Tuesday", "Thursday"), "09:00", "10:15", "Science Building 201", 25, 0, "Spring 2026"));
        courses.add(new Course("CHEM101", "General Chemistry", "Atomic structure, bonding, and chemical reactions", "Prof. Jessica Lee", 4, List.of("Monday", "Wednesday", "Friday"), "11:00", "12:00", "Science Building 305", 30, 0, "Spring 2026"));
        courses.add(new Course("BIO101", "Biology I", "Cell biology, genetics, and organisms", "Dr. Thomas Davis", 4, List.of("Tuesday", "Thursday"), "11:00", "12:15", "Life Sciences 110", 35, 0, "Spring 2026"));
        courses.add(new Course("HIST101", "World History", "Major events and civilizations throughout history", "Prof. Amanda Rodriguez", 3, List.of("Monday", "Wednesday"), "15:00", "16:15", "Humanities Building 205", 40, 0, "Spring 2026"));
        return courses;
    }

    private List<User> defaultUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("admin1", "admin", "admin123", "Admin User", "admin@university.edu", "admin"));
        users.add(new User("student1", "john", "john123", "John Doe", "john@university.edu", "student"));
        users.add(new User("student2", "jane", "jane123", "Jane Smith", "jane@university.edu", "student"));
        users.add(new User("student3", "mike", "mike123", "Mike Johnson", "mike@university.edu", "student"));
        return users;
    }

    public String nextId() {
        return UUID.randomUUID().toString();
    }

    public String now() {
        return Instant.now().toString();
    }
}
